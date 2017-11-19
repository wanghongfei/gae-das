package org.fh.gae.das.template.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Template {
    private List<Table> tableList;
}
