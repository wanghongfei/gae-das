package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * listener继承此类, 可获得event的库名, 表名信息
 * @param <T>
 */
@Slf4j
public class AggregationListener<T> implements BinaryLogClient.EventListener {
    private EventType targetEventType;

    private String dbName;
    private String tableName;


    /**
     * 构造对象时指定感兴趣的事件类型
     * @param eventType
     */
    protected AggregationListener(EventType eventType) {
        this.targetEventType = eventType;
    }

    /**
     * 子类覆盖此方法
     * @param eventData 事件数据
     * @param dbName 库名
     * @param tableName 表名
     */
    protected void doEvent(T eventData, String dbName, String tableName) {
        return;
    }

    @Override
    public void onEvent(Event event) {
        EventType type = event.getHeader().getEventType();
        // 缓存表名和库名
        if (type == EventType.TABLE_MAP) {
            onTableMap(event);
        }

        // 触发子类doEvent()方法, 传递表名库名
        if (type == targetEventType) {
            if (StringUtils.isEmpty(dbName) || StringUtils.isEmpty(tableName)) {
                log.error("no meta data event");
                return;
            }

            log.info("trigger event {}", type.name());
            T data = event.getData();

            String dbName = this.dbName;
            String tabName = this.tableName;

            this.dbName = "";
            this.tableName = "";

            doEvent(data, dbName, tabName);
        }
    }

    private void onTableMap(Event event) {
        TableMapEventData data = event.getData();
        this.tableName = data.getTable();
        this.dbName = data.getDatabase();
    }

}
