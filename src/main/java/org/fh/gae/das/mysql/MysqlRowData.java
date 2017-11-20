package org.fh.gae.das.mysql;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.fh.gae.das.template.DasTable;

import java.util.Map;

@Data
@NoArgsConstructor
public class MysqlRowData {
    private DasTable table;

    private Map<String, String> after;

    private Map<String, String> before;
}
