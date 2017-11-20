package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.sender.FileSender;
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
public class UpdateEventListener extends AggregationListener {
    @Autowired
    private TemplateHolder holder;

    @Autowired
    private FileSender store;

    public UpdateEventListener() {
        super(EventType.UPDATE_ROWS);
    }

    @Override
    protected String getDbName() {
        return "gae-das";
    }

    @Override
    protected String getTargetTable() {
        return "new_table";
    }

    @Override
    protected void doEvent(MysqlRowData eventData, String dbName, String tableName) {
        if (null == eventData) {
            return;
        }

        System.out.println(dbName + ", " + tableName + ", " + eventData);

        DasTable table = holder.getTable(tableName);
        if (null == table) {
            log.warn("table {} not found", tableName);
            return;
        }

        // 构造层级对象
        DasLevel level = new TextDasLevel();
        level.setTable(table);
        level.setOpType(OpType.UPDATE);

        // 取出模板中UPDATE操作对应的字段列表
        List<String> fieldList = table.getOpTypeFieldSetMap().get(OpType.UPDATE);
        if (null == fieldList) {
            log.warn("UPDATE not support for {}", tableName);
            return;
        }

        for (Map.Entry<String, String> entry : eventData.getAfter().entrySet()) {
            String colName = entry.getKey();
            String colValue = entry.getValue();


            level.getFieldValueMap().put(colName, colValue);
        }

        store.send(level);
    }

    @Override
    protected TemplateHolder getTemplateHolder() {
        return this.holder;
    }
}
