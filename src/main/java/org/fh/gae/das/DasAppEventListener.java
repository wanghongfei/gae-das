package org.fh.gae.das;

import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.mysql.BinlogClient;
import org.fh.gae.das.mysql.MysqlBinlogConfig;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;

import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class DasAppEventListener implements ApplicationListener<ApplicationContextEvent> {
    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextClosedEvent) {
            // 上下文关闭时
            // 保存binlog位置信息到文件
            BinlogClient client = event.getApplicationContext().getBean(BinlogClient.class);
            String data = client.getBinlogName() + ":" + client.getBinlogPos();

            MysqlBinlogConfig config = event.getApplicationContext().getBean(MysqlBinlogConfig.class);
            String filename = config.getBinlogPositionFile();
            writeData(filename, data);

            log.info("binlog position saved. {}", data);
        }
    }

    private void writeData(String file, String data) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
