package org.fh.gae.das.sender;

import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.exception.DasStoreException;
import org.fh.gae.das.template.DasSerializable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "das.store.file", name = "enable", matchIfMissing = true, havingValue = "true")
@Slf4j
public class FileSender implements DasSender {
    @Value("${das.store.file.path}")
    private String filename;

    private BufferedOutputStream out;

    @PostConstruct
    private void init() throws IOException {
        log.info("index file:{}", filename);

        FileOutputStream fos = new FileOutputStream(filename, true);
        out = new BufferedOutputStream(fos);
    }

    @Override
    public void send(DasSerializable data) {
        byte[] buf = data.serialize();

        try {
            out.write(buf);
            out.write('\n');
            out.flush();

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DasStoreException("fail to write file");
        }
    }
}
