package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.ha.CoordinationService;
import org.fh.gae.das.ha.heartbeat.BeatTimeHolder;
import org.fh.gae.das.mysql.binlog.BinlogPosition;
import org.fh.gae.das.mysql.binlog.BinlogPositionStore;
import org.fh.gae.das.mysql.listener.AggregationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * 连接mysql
 */
@Component
@Slf4j
public class BinlogClient {
    @Autowired
    private MysqlBinlogConfig config;

    private BinaryLogClient client;

    @Autowired
    private BinlogPositionStore positionStore;

    @Autowired
    private AggregationListener listener;

    @Autowired
    private CoordinationService coordinationService;

    public void connect() {
        new Thread(() -> {
            client = new BinaryLogClient(
                    config.getHost(),
                    config.getPort(),
                    config.getUsername(),
                    config.getPassword()
            );

            BinlogPosition position = resetBinlogPositionInOrder();
            if (null != position) {
                log.info("starting from previous position {}", position);
            }


            client.registerEventListener(listener);
            client.setServerId(config.getServerId());


            try {
                log.info("connecting to mysql");
                client.connect();
                log.info("connection to mysql closed");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void close() {
        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BinlogPosition resetBinlogPositionInOrder() {
        // 先检查是否有心跳包中的位置信息
        if (null != BeatTimeHolder.position) {
            return BeatTimeHolder.position;
        }

        // 从文件中加载
        BinlogPosition binlogPosition = positionStore.load();
        if (null != binlogPosition) {
            client.setBinlogFilename(binlogPosition.getBinlogName());
            client.setBinlogPosition(binlogPosition.getPosition());
            return binlogPosition;
        }

        // 从配置文件中加载
        String binlogName = config.getBinlogName();
        long pos = config.getPosition().longValue();
        if (!binlogName.isEmpty() && -1 != pos) {
            client.setBinlogFilename(binlogName);
            client.setBinlogPosition(pos);

            return new BinlogPosition(binlogName, pos);
        }

        return null;
    }

    public long getBinlogPos() {
        return client.getBinlogPosition();
    }

    public String getBinlogName() {
        return client.getBinlogFilename();
    }
}
