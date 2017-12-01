package org.fh.gae.das.mysql.listener;

import org.fh.gae.das.mysql.MysqlRowData;

public interface BizListener {
    void onEvent(MysqlRowData eventData);
}
