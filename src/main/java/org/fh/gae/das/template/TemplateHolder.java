package org.fh.gae.das.template;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.exception.InvalidDasTemplateException;
import org.fh.gae.das.template.vo.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TemplateHolder {
    private DasTemplate dasTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String SQL_SCHEMA = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";

    @PostConstruct
    private void init() {
        loadJson("template.json");
    }

    /**
     * 加载配置文件
     * @param path
     */
    public void loadJson(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream inStream = cl.getResourceAsStream(path);

        try {
            Template template = JSON.parseObject(inStream, Charset.defaultCharset(), Template.class);
            this.dasTemplate = DasTemplate.parse(template);
            loadMeta();

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InvalidDasTemplateException("fail to parse json file");
        }
    }

    public DasTable getTable(String tableName) {
        return dasTemplate.getTableMap().get(tableName);
    }

    /**
     * 查询模板中每张表的schema信息,因为binlog中不包含列名信息
     */
    private void loadMeta() {
        String db = dasTemplate.getDatabase();


        for (Map.Entry<String, DasTable> entry : dasTemplate.getTableMap().entrySet()) {
            DasTable table = entry.getValue();
            String tableName = table.getTableName();

            List<String> updateFields = table.getOpTypeFieldSetMap().get(OpType.UPDATE);
            List<String> insertFields = table.getOpTypeFieldSetMap().get(OpType.ADD);
            List<String> deleteFields = table.getOpTypeFieldSetMap().get(OpType.DELETE);

            jdbcTemplate.query(SQL_SCHEMA, new Object[]{db, tableName}, (rs, i) -> {
                int pos = rs.getInt("ORDINAL_POSITION");
                String colName = rs.getString("COLUMN_NAME");

                if (updateFields.contains(colName)
                        || insertFields.contains(colName)
                        || deleteFields.contains(colName)) {

                    table.getPosMap().put(pos - 1, colName);
                }

                return null;
            });
        }
    }
}
