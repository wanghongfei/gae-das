package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.event.EventType;
import org.fh.gae.das.template.TemplateHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InsertEventListener extends AggregationListener {
    @Autowired
    private TemplateHolder holder;

    public InsertEventListener() {
        super(EventType.WRITE_ROWS);
    }

    @Override
    protected String getDbName() {
        return "gae-das";
    }

    @Override
    protected String getTargetTable() {
        return "acc";
    }

    @Override
    protected void doEvent(MysqlRowData eventData, String dbName, String tableName) {
        if (null == eventData) {
            return;
        }

        System.out.println(dbName + ", " + tableName + ", " + eventData);
    }

    @Override
    protected TemplateHolder getTemplateHolder() {
        return this.holder;
    }
}
