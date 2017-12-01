package org.fh.gae.das.mysql.listener;

import com.github.shyiko.mysql.binlog.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.mysql.MysqlRowData;
import org.fh.gae.das.sender.file.FileSender;
import org.fh.gae.das.template.DasTable;
import org.fh.gae.das.template.OpType;
import org.fh.gae.das.template.level.DasLevel;
import org.fh.gae.das.template.level.TextDasLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DemoTableListener implements BizListener {
    @Autowired
    private AggregationListener aggregationListener;

    @Autowired
    private FileSender store;

    @PostConstruct
    private void register() {
        aggregationListener.register("gae-das", "acc", this);
    }

    @Override
    public void onEvent(MysqlRowData eventData) {
        log.info(eventData.toString());

        DasTable table = eventData.getTable();
        EventType eventType = eventData.getEventType();

        // 构造层级对象
        DasLevel level = new TextDasLevel();
        level.setTable(table);
        OpType opType = OpType.of(eventType);
        level.setOpType(opType);

        // 取出模板中该操作对应的字段列表
        List<String> fieldList = table.getOpTypeFieldSetMap().get(opType);
        if (null == fieldList) {
            log.warn("{} not support for {}", opType, table.getTableName());
            return;
        }

        for (Map.Entry<String, String> entry : eventData.getAfter().entrySet()) {
            String colName = entry.getKey();
            String colValue = entry.getValue();

            level.getFieldValueMap().put(colName, colValue);
        }

        store.send(level);
    }
}
