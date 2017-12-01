package org.fh.gae.das.mysql.binlog;

public interface BinlogPositionStore {
    /**
     * 加载上次保存的binlog pos
     * @return
     */
    BinlogPosition load();

    /**
     * 保存当前binlog位置
     * @param binlogPosition
     * @return
     */
    int save(BinlogPosition binlogPosition);

    /**
     * 获取当前binlog位置
     * @return
     */
    BinlogPosition extract();
}
