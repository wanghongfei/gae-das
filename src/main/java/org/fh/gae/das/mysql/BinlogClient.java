package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * 连接mysql
 */
@Component
@Slf4j
public class BinlogClient {
    @Value("${das.mysql.host}")
    private String host;

    @Value("${das.mysql.port}")
    private int port;

    @Value("${das.mysql.username}")
    private String username;

    @Value("${das.mysql.password}")
    private String password;

    @Autowired
    private UpdateEventListener updateEventListener;

    @Autowired
    private InsertEventListener insertEventListener;

    @PostConstruct
    public void connect() throws IOException {
        new Thread(() -> {
            BinaryLogClient logClient = new BinaryLogClient(host, port, username, password);
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
