package org.fh.gae.das.ha.heartbeat;

import org.fh.gae.das.mysql.binlog.BinlogPosition;

public class BeatTimeHolder {
    public static volatile long lastBeat = 0;

    public static volatile BinlogPosition position;
}
