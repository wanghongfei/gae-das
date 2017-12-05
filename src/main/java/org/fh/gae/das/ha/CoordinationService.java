package org.fh.gae.das.ha;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.fh.gae.das.ha.heartbeat.BeatMessage;
import org.fh.gae.das.mysql.BinlogClient;
import org.fh.gae.das.mysql.binlog.BinlogPosition;
import org.fh.gae.das.mysql.binlog.BinlogPositionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * 协调服务, 与peer节点"讨论"谁是master
 */
@Component
@Slf4j
public class CoordinationService {
    private CloseableHttpClient httpClient;

    @Value("${das.ha.peer-host}")
    private String peerHost;

    @Value("${das.ha.peer-port}")
    private int peerPort;

    @Autowired
    private BinlogClient binlogClient;

    @Autowired
    private BinlogPositionStore positionStore;

    /**
     * 当前实例状态
     */
    private volatile Status status;

    @PostConstruct
    private void initClient() {
        httpClient = HttpClients.createDefault();
    }

    public void startBinlogClient() {
        boolean master = tryMaster();
        if (master) {
            log.info("change status to MASTER");
            binlogClient.connect();
            return;
        }

        boolean slave = trySlave();
        if (slave) {
            log.info("change status to SLAVE");
        }
    }

    /**
     * 尝试让自己变成master
     *
     * @return true表示成功变为master
     */
    public synchronized boolean tryMaster() {
        log.info("trying MASTER...");
        boolean master = doTryMaster("", -100);
        if (master) {
            this.status = Status.MASTER;
            return true;
        }

        return false;
    }

    public boolean heartbeat() {
        BinlogPosition position = positionStore.load();
        return sendReport(position.getBinlogName(), position.getPosition());
    }

    public void changeStatus(Status newStatus) {
        if (this.status == Status.SLAVE && newStatus == Status.MASTER) {
            binlogClient.connect();

        } else if (this.status == Status.MASTER && newStatus == Status.SLAVE) {
            binlogClient.close();
        }

        this.status = newStatus;
    }

    private boolean doTryMaster(String binlog, long pos) {
        return sendReport(binlog, pos);
    }

    /**
     * 尝试变成slave
     * @return true表示成功
     */
    public synchronized boolean trySlave() {
        if (null == status) {
            status = Status.SLAVE;
            return true;
        }

        return false;
    }

    public synchronized Status status() {
        return this.status;
    }

    /**
     * 发送HTTP请求报告自己binlog位置
     * @param binlog
     * @param pos
     * @return true表示自己可成为master, fales表示不可成为master
     */
    private boolean sendReport(String binlog, long pos) {
        BeatMessage msg = BeatMessage.Builder.buildReport(binlog, pos);

        HttpPost post = new HttpPost("http://" + peerHost + ":" + peerPort);
        StringEntity body = new StringEntity(JSON.toJSONString(msg), Charset.defaultCharset());
        post.setEntity(body);

        Random random = new Random();
        int timeout = random.nextInt(1500) + 100;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
        post.setConfig(config);


        try (CloseableHttpResponse response = httpClient.execute(post)) {
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);

            BeatMessage respMsg = JSON.parseObject(json, BeatMessage.class);
            // peer确认
            if (BeatMessage.MessageType.ACK.code() == respMsg.getType()) {
                log.info("ACK received");
                return true;
            }

            // 对方已经处于MASTER状态了
            if (BeatMessage.MessageType.MASTER.code() == respMsg.getType()) {
                this.status = Status.SLAVE;
                return false;
            }

        } catch (Exception e) {
            // peer没收到
            log.warn(e.getMessage());
            return true;
        }

        return false;
    }


    public enum Status {
        MASTER,
        SLAVE;
    }
}
