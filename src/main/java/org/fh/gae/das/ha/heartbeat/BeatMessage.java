package org.fh.gae.das.ha.heartbeat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 心跳数据定义
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeatMessage {
    private int type;

    private String binlog;

    private long position;

    public static class Builder {
        public static BeatMessage buildReport(String binlog, long pos) {
            return new BeatMessage(MessageType.REPORT.code, binlog, pos);
        }

        public static BeatMessage buildAck() {
            return new BeatMessage(MessageType.ACK.code, "", 0);
        }

        public static BeatMessage buildUnknown() {
            return new BeatMessage(MessageType.UNKNOWN.code, "", 0);
        }

        public static BeatMessage buildMaster() {
            return new BeatMessage(MessageType.MASTER.code, "", 0);
        }
    }

    public static enum MessageType {
        REPORT(1),
        ACK(0),
        MASTER(2),

        UNKNOWN(-1);

        private int code;

        MessageType(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }

        public static MessageType of(int code) {
            switch (code) {
                case 1:
                    return REPORT;
                case 0:
                    return ACK;
                case 2:
                    return MASTER;
            }

            return UNKNOWN;
        }
    }
}
