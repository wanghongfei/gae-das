package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.sender.FileSender;
import org.fh.gae.das.template.DasTable;
import org.fh.gae.das.template.OpType;
import org.fh.gae.das.template.TemplateHolder;
import org.fh.gae.das.template.level.DasLevel;
import org.fh.gae.das.template.level.TextDasLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UpdateEventListener extends AggregationListener<UpdateRowsEventData> {
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
    protected void doEvent(UpdateRowsEventData eventData, String dbName, String tableName) {
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

        // 遍历行
        for (Map.Entry<Serializable[], Serializable[]> entry : eventData.getRows()) {
            // 取出新值
            Serializable[] after = entry.getValue();
            int colLen = after.length;

            // 遍历值
            for (int ix = 0; ix < colLen; ++ix) {
                // 取出当前位置对应的列名
                String colName = table.getPosMap().get(ix);
                // 如果没有则说明不关心此列
                if (null == colName) {
                    if (log.isDebugEnabled()) {
                        log.debug("ignore position: {}", ix);
                    }

                    continue;
                }

                String colValue = after[ix].toString();
                level.getFieldValueMap().put(colName, colValue);
            }
        }

        store.send(level);
    }
}
