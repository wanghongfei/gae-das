package org.fh.gae.das.mysql.binlog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fh.gae.das.template.DasSerializable;
import org.springframework.util.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BinlogPosition implements DasSerializable {
    private String binlogName = "";

    private long position = -1;

    public String toString() {
        return this.binlogName + ":" + position;
    }

    @Override
    public byte[] serialize() {
        return toString().getBytes();
    }

    public static BinlogPosition deserialize(byte[] buf, int start, int len) {
        return deserialize(new String(buf, start, len));
    }

    public static BinlogPosition deserialize(String data) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }

        String[] terms = data.split(":");
        if (terms.length != 2) {
            return null;
        }

        String name = terms[0];
        long pos = Long.valueOf(terms[1]);

        return new BinlogPosition(name, pos);
    }
}
