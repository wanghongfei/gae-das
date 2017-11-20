package org.fh.gae.das.template;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DasTable {
    private String tableName;

    private String level;

    /**
     * 操作类型->字段顺序
     */
    private Map<OpType, List<String>> opTypeFieldSetMap = new HashMap<>();

    /**
     * 字段位置->字段名
     */
    private Map<Integer, String> posMap = new HashMap<>();
}
