package org.fh.gae.das.mysql.binlog;

import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.mysql.BinlogClient;
import org.fh.gae.das.mysql.MysqlBinlogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;

@Component
@Slf4j
public class FileBinlogPositionStore implements BinlogPositionStore {
    @Autowired
    protected BinlogClient client;

    @Autowired
    private MysqlBinlogConfig config;

    private byte[] loadBuf = new byte[512];

    @Override
    public BinlogPosition load() {
        try (FileInputStream in = new FileInputStream(config.getBinlogPositionFile())) {
            int len = in.read(loadBuf);
            return BinlogPosition.deserialize(loadBuf, 0, len);

        } catch (Exception e) {
            log.error("binlog position failed to load, {}", e.getMessage());
        }

        return null;
    }

    @Override
    public int save(BinlogPosition binlogPosition) {
        try (FileOutputStream out = new FileOutputStream(config.getBinlogPositionFile())) {
            out.write(binlogPosition.serialize());

        } catch (Exception e) {
            log.error("binlog position failed to save, {}", e);

            return -1;
        }


        log.debug("binlog position {}:{} saved", binlogPosition.getBinlogName(), binlogPosition.getPosition());
        return 0;
    }

    @Override
    public BinlogPosition extract() {
        return new BinlogPosition(
                client.getBinlogName(),
                client.getBinlogPos()
        );
    }
}
