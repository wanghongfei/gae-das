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
    private UpdateEventListener updateEventListener;

    @Autowired
    private InsertEventListener insertEventListener;

    @PostConstruct
    public void connect() throws IOException {
        new Thread(() -> {
            BinaryLogClient logClient = new BinaryLogClient("127.0.0.1", 3306, "root", "");
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
