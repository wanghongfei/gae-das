package org.fh.gae.das.template;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.exception.InvalidDasTemplateException;
import org.fh.gae.das.template.vo.Template;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Component
@Slf4j
public class TemplateHolder {
    private DasTemplate dasTemplate;

    @PostConstruct
    public void init() {
        loadJson("template.json");
    }

    public void loadJson(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream inStream = cl.getResourceAsStream(path);

        try {
            Template template = JSON.parseObject(inStream, Charset.defaultCharset(), Template.class);
            this.dasTemplate = DasTemplate.parse(template);

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InvalidDasTemplateException("fail to parse json file");
        }
    }

    public DasTable getTable(String tableName) {
        return dasTemplate.getTableMap().get(tableName);
    }
}
