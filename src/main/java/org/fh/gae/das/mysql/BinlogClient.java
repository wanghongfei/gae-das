package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    private UpdateEventListener updateEventListener;

    @Autowired
    private InsertEventListener insertEventListener;

    @PostConstruct
    public void connect() throws IOException {
        new Thread(() -> {
            BinaryLogClient logClient = new BinaryLogClient(
                    config.getHost(),
                    config.getPort(),
                    config.getUsername(),
                    config.getPassword()
            );

            String binlogName = config.getBinlogName();
            if (!binlogName.isEmpty()) {
                logClient.setBinlogFilename(binlogName);
            }

            long pos = config.getPosition().longValue();
            if (pos != -1) {
                logClient.setBinlogPosition(pos);
            }

            logClient.registerEventListener(updateEventListener);
            logClient.registerEventListener(insertEventListener);

            try {
                logClient.connect();
                log.info("mysql connected");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }
}
