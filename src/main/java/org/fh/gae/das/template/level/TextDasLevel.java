package org.fh.gae.das.template.level;

import lombok.extern.slf4j.Slf4j;
import org.fh.gae.das.template.OpType;

@Slf4j
public class TextDasLevel extends DasLevel {
    @Override
    public byte[] serialize() {
        // 操作类型
        OpType opType = getOpType();

        int valueSize = getFieldValueMap().size();

        StringBuilder sb = new StringBuilder(valueSize * 10);
        sb.append(getTable().getLevel()).append("\t");
        sb.append(opType.ordinal()).append("\t");

        // 遍历当前层级对应的操作类型的所有字段
        for (String fieldName : getTable().getOpTypeFieldSetMap().get(opType)) {
            // 取出字段值
            String fieldValue = getFieldValueMap().get(fieldName);
            if (null == fieldValue) {
                log.warn("field {} have no value", fieldName);
                continue;
            }

            sb.append(fieldValue);
            sb.append("\t");
        }

        int len = sb.length();
        sb.replace(len - 1, len, "");

        return sb.toString().getBytes();
    }
}
