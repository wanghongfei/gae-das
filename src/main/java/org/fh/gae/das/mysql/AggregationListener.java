package org.fh.gae.das.mysql;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.template.DasTable;
import org.fh.gae.das.template.TemplateHolder;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * listener继承此类, 可获得event的库名, 表名信息
 */
@Slf4j
public abstract class AggregationListener implements BinaryLogClient.EventListener {
    private EventType targetEventType;

    private String dbName;
    private String tableName;


    /**
     * 构造对象时指定感兴趣的事件类型
     * @param eventType 传null表示对所有事件感兴趣
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
    protected abstract void doEvent(MysqlRowData eventData, String dbName, String tableName);

    /**
     * 子类覆盖此方法, 提供TemplateHolder对象
     * @return
     */
    protected abstract TemplateHolder getTemplateHolder();

    /**
     * 感兴趣的库名
     * @return
     */
    protected abstract String getDbName();

    /**
     * 感兴趣的表名
     * @return
     */
    protected abstract String getTargetTable();

    @Override
    public void onEvent(Event event) {
        EventType type = event.getHeader().getEventType();
        // 缓存表名和库名
        if (type == EventType.TABLE_MAP) {
            onTableMap(event);
            return;
        }


        // 触发子类doEvent()方法, 传递表名库名
        if (type == targetEventType || null == targetEventType) {
            if (StringUtils.isEmpty(dbName) || StringUtils.isEmpty(tableName)) {
                log.error("no meta data event");
                return;
            }

            if (!getDbName().equals(dbName) || !getTargetTable().equals(tableName)) {
                log.info("filter {}:{}", dbName, tableName);
                return;
            }

            log.info("trigger event {}", type.name());

            try {
                doEvent(buildRowData(event.getData()), dbName, tableName);

            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                this.dbName = "";
                this.tableName = "";
            }
        }
    }

    private void onTableMap(Event event) {
        TableMapEventData data = event.getData();
        this.tableName = data.getTable();
        this.dbName = data.getDatabase();
    }

    /**
     * 从binlog对象中取出最新的值列表
     * @param eventData
     * @return
     */
    private List<Serializable[]> getAfterValues(EventData eventData) {
        if (eventData instanceof WriteRowsEventData) {
            return ((WriteRowsEventData) eventData).getRows();
        }

        if (eventData instanceof UpdateRowsEventData) {
            return ((UpdateRowsEventData) eventData).getRows().stream()
                    .map( entry -> entry.getValue() )
                    .collect(Collectors.toList());
        }

        if (eventData instanceof DeleteRowsEventData) {
            return ((DeleteRowsEventData) eventData).getRows();
        }

        return Collections.emptyList();
    }

    /**
     * 将Biglog数据对象转换成MysqlRowData对象
     * @param eventData
     * @return
     */
    private MysqlRowData buildRowData(EventData eventData) {
        DasTable table = getTemplateHolder().getTable(tableName);
        if (null == table) {
            log.warn("table {} not found", tableName);
            return null;
        }

        Map<String, String> afterMap = new HashMap<>();
        // 遍历行
        for (Serializable[] after : getAfterValues(eventData)) {
            // 取出新值
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

                afterMap.put(colName, colValue);
            }
        }

        MysqlRowData rowData = new MysqlRowData();
        rowData.setAfter(afterMap);

        return rowData;

    }


}
