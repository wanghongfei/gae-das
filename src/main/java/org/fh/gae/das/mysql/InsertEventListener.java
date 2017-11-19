package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import org.springframework.stereotype.Component;

@Component
public class InsertEventListener extends AggregationListener<WriteRowsEventData> {
    public InsertEventListener() {
        super(EventType.WRITE_ROWS);
    }

    @Override
    protected void doEvent(WriteRowsEventData eventData, String dbName, String tableName) {
        System.out.println(dbName + ", " + tableName + ", " + eventData);
    }
}
