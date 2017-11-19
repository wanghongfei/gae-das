package org.fh.gae.das.template.level;

import lombok.Data;
import org.fh.gae.das.template.DasSerializable;
import org.fh.gae.das.template.DasTable;
import org.fh.gae.das.template.OpType;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class DasLevel implements DasSerializable {
    protected DasTable table;

    protected OpType opType;

    protected Map<String, String> fieldValueMap = new HashMap<>();
}
