package org.fh.gae.das.test;

import org.fh.gae.das.template.level.DasLevel;
import org.fh.gae.das.template.DasTable;
import org.fh.gae.das.template.OpType;
import org.fh.gae.das.template.level.TextDasLevel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextDasLevelTest {
    @Test
    public void testSerialize() {
        DasLevel dasLevel = new TextDasLevel();
        dasLevel.setOpType(OpType.ADD);

        Map<OpType, List<String>> fields = new HashMap<>();
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id");
        fieldList.add("name");
        fieldList.add("age");
        fields.put(OpType.ADD, fieldList);
        DasTable table = new DasTable();
        table.setOpTypeFieldSetMap(fields);
        table.setTableName("gae");
        table.setLevel("1");
        dasLevel.setTable(table);

        Map<String, String> values = new HashMap<>();
        values.put("name", "gae");
        values.put("id", "1");
        values.put("age", "24");
        dasLevel.setFieldValueMap(values);

        byte[] buf = dasLevel.serialize();
        System.out.println(new String(buf, 0, buf.length));
    }
}
