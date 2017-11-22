package org.fh.gae.das.sender.file;

import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.exception.DasStoreException;
import org.fh.gae.das.sender.DasSender;
import org.fh.gae.das.template.DasSerializable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 将增量数据保存到文件中;
 * 非线程安全，时刻只能有一个线程运行
 */
@Component
@ConditionalOnProperty(prefix = "das.store.file", name = "enable", matchIfMissing = true, havingValue = "true")
@Slf4j
public class FileSender implements DasSender {
    @Value("${das.store.file.path}")
    private String filename;

    /**
     * 增量文件索引
     */
    private int fileIndex = 0;

    /**
     * 当前增量文件行数
     */
    private int rowNumber = 0;

    /**
     * 一个增量文件最大行数
     */
    private int maxRowNumber = 10 * 10000;
    // private int maxRowNumber = 2;

    private BufferedOutputStream out;

    @PostConstruct
    private void init() throws IOException {
        findNextFileIndex();

        String name = filename + "." + fileIndex;
        log.info("index file:{}", name);

        FileOutputStream fos = new FileOutputStream(name, true);
        out = new BufferedOutputStream(fos);
    }

    @Override
    public void send(DasSerializable data) {
        checkRowNumber();
        byte[] buf = data.serialize();

        try {
            out.write(buf);
            out.write('\n');
            out.flush();

            ++rowNumber;

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DasStoreException("fail to write file");
        }
    }

    private void findNextFileIndex() {
        for (int ix = 0; ix < 10000; ++ix) {
            File f = new File(this.filename + "." + ix);
            if (!f.exists()) {
                this.fileIndex = ix;
                break;
            }
        }
    }

    /**
     * 如果行数大于最大值, 则创建新文件, 文件索引数加1
     */
    private void checkRowNumber() {
        if (rowNumber < maxRowNumber) {
            return;
        }

        findNextFileIndex();
        try {
            String name = this.filename + "." + this.fileIndex;

            FileOutputStream fos = new FileOutputStream(name, true);
            out = new BufferedOutputStream(fos);

            this.rowNumber = 0;

            log.info("switch to new index file: {}", name);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }


    }
}
