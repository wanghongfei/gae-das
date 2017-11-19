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
    protected void doEvent(UpdateRowsEventData eventData, String dbName, String tableName) {
        System.out.println(dbName + ", " + tableName + ", " + eventData);

        DasTable table = holder.getTable(tableName);
        if (null == table) {
            log.warn("table {} not found", tableName);
            return;
        }

        DasLevel level = new TextDasLevel();
        level.setTable(table);
        level.setOpType(OpType.UPDATE);

        List<String> fieldList = table.getOpTypeFieldSetMap().get(OpType.UPDATE);
        if (null == fieldList) {
            log.warn("UPDATE not support for {}", tableName);
            return;
        }

        for (Map.Entry<Serializable[], Serializable[]> entry : eventData.getRows()) {
            Serializable[] after = entry.getValue();

            for (int ix = 0; ix < after.length; ++ix) {
                String colName = fieldList.get(ix);
                level.getFieldValueMap().put(colName, after[ix].toString());
            }
        }

        store.send(level);
    }
}
