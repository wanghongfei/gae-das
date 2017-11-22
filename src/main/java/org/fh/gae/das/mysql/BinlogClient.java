package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.mysql.listener.DeleteEventListener;
import org.fh.gae.das.mysql.listener.InsertEventListener;
import org.fh.gae.das.mysql.listener.UpdateEventListener;
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
    private UpdateEventListener updateEventListener;

    @Autowired
    private InsertEventListener insertEventListener;

    @Autowired
    private DeleteEventListener deleteEventListener;

    @PostConstruct
    public void connect() throws IOException {
        new Thread(() -> {
            client = new BinaryLogClient(
                    config.getHost(),
                    config.getPort(),
                    config.getUsername(),
                    config.getPassword()
            );

            String binlogName = config.getBinlogName();
            if (!binlogName.isEmpty()) {
                client.setBinlogFilename(binlogName);
            }

            long pos = config.getPosition().longValue();
            if (pos != -1) {
                client.setBinlogPosition(pos);
            }

            client.registerEventListener(updateEventListener);
            client.registerEventListener(insertEventListener);
            client.registerEventListener(deleteEventListener);

            try {
                client.connect();
                log.info("mysql connected");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public long getBinlogPos() {
        return client.getBinlogPosition();
    }

    public String getBinlogName() {
        return client.getBinlogFilename();
    }
}
