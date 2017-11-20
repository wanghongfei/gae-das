package org.fh.gae.das.mysql.listener;

import com.github.shyiko.mysql.binlog.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.mysql.MysqlRowData;
import org.fh.gae.das.sender.file.FileSender;
import org.fh.gae.das.template.DasTable;
import org.fh.gae.das.template.OpType;
import org.fh.gae.das.template.TemplateHolder;
import org.fh.gae.das.template.level.DasLevel;
import org.fh.gae.das.template.level.TextDasLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DeleteEventListener extends AggregationListener {
    @Autowired
    private TemplateHolder holder;

    @Autowired
    private FileSender fileSender;

    public DeleteEventListener() {
        super(EventType.DELETE_ROWS);
    }


    @Override
    protected void doEvent(MysqlRowData eventData) {
        DasTable table = eventData.getTable();

        // 构造层级对象
        DasLevel level = new TextDasLevel();
        level.setTable(table);
        level.setOpType(OpType.DELETE);

        // 取出模板中DELETE操作对应的字段列表
        List<String> fieldList = table.getOpTypeFieldSetMap().get(OpType.DELETE);
        if (null == fieldList) {
            log.warn("DELETE not support for {}", table.getTableName());
            return;
        }

        for (Map.Entry<String, String> entry : eventData.getAfter().entrySet()) {
            String colName = entry.getKey();
            String colValue = entry.getValue();


            level.getFieldValueMap().put(colName, colValue);
        }

        fileSender.send(level);

    }

    @Override
    protected TemplateHolder getTemplateHolder() {
        return this.holder;
    }

    @Override
    protected String getDbName() {
        return "gae-das";
    }
}
