package org.fh.gae.das;

import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.ha.HaServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

@Slf4j
public class DasAppEventListener implements ApplicationListener<ApplicationContextEvent> {
    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // 上下文关闭时
        if (event instanceof ContextClosedEvent) {
            return;
        }

        // 上下文初始化完毕
        if (event instanceof ContextRefreshedEvent) {
            HaServer haServer = event.getApplicationContext().getBean(HaServer.class);

            // 启动心跳服务器
            haServer.start();
            // coordinationService.startBinlogClient();
            // 启动binlog client
            // binlogClient.connect();
        }
    }

}
