package org.fh.gae.das.ha.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.ha.CoordinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class BeatTask implements Runnable {
    @Autowired
    private CoordinationService coordinationService;

    @Override
    public void run() {
        if (null == coordinationService.status()) {
            // 尝试变成master
            coordinationService.startBinlogClient();

            return;
        }

        if (CoordinationService.Status.MASTER == coordinationService.status()) {
            // 发送心跳
            log.info("sending heartbeat");
            coordinationService.heartbeat();

            return;
        }

        if (CoordinationService.Status.SLAVE == coordinationService.status()) {
            // 检查上次心跳间隔
            if (new Date().getTime() - BeatTimeHolder.lastBeat >= TimeUnit.SECONDS.toMillis(5)) {
                log.info("no beat received past 5s, change to master");
                coordinationService.startBinlogClient();
            }
        }
    }
}
