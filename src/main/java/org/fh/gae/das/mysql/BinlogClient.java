package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import lombok.extern.slf4j.Slf4j;
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

    @PostConstruct
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

            try {
                client.connect();
                log.info("mysql connected");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private BinlogPosition resetBinlogPositionInOrder() {
        // 先从文件中加载
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
