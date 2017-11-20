package org.fh.gae.das.template;

import lombok.Data;
import org.fh.gae.das.template.vo.Column;
import org.fh.gae.das.template.vo.Table;
import org.fh.gae.das.template.vo.Template;
import org.fh.gae.das.utils.GaeCollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DasTemplate {
    private String database;

    private Map<String, DasTable> tableMap = new HashMap<>();

    public static DasTemplate parse(Template temp) {
        DasTemplate dasTemplate = new DasTemplate();
        dasTemplate.setDatabase(temp.getDatabase());

        // 遍历表
        for (Table table : temp.getTableList()) {
            String name = table.getTableName();
            Integer level = table.getLevel();

            DasTable dasTable = new DasTable();
            dasTable.setTableName(name);
            dasTable.setLevel(level.toString());
            dasTemplate.tableMap.put(name, dasTable);

            // 遍历列
            Map<OpType, List<String>> opTypeFieldSetMap = dasTable.getOpTypeFieldSetMap();

            for (Column column : table.getInsert()) {
                String colName = column.getColumn();

                GaeCollectionUtils.getAndCreateIfNeed(
                        OpType.ADD,
                        opTypeFieldSetMap,
                        () -> new ArrayList<>()
                ).add(colName);
            }

            for (Column column : table.getUpdate()) {
                String colName = column.getColumn();

                GaeCollectionUtils.getAndCreateIfNeed(
                        OpType.UPDATE,
                        opTypeFieldSetMap,
                        () -> new ArrayList<>()
                ).add(colName);
            }

            for (Column column : table.getDelete()) {
                String colName = column.getColumn();

                GaeCollectionUtils.getAndCreateIfNeed(
                        OpType.DELETE,
                        opTypeFieldSetMap,
                        () -> new ArrayList<>()
                ).add(colName);
            }
        }

        return dasTemplate;
    }
}
