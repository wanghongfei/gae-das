package org.fh.gae.das.template;

import com.github.shyiko.mysql.binlog.event.EventType;
import org.fh.gae.das.exception.InvalidDasTemplateException;

public enum OpType {
    ADD,
    UPDATE,
    DELETE,
    OTHER;

    public static OpType of(String str) {
        switch (str) {
            case "insert":
                return ADD;

            case "update":
                return UPDATE;

            case "delete":
                return DELETE;
        }

        throw new InvalidDasTemplateException("invalid type: " + str);
    }

    public static OpType of(EventType eventType) {
        switch (eventType) {
            case WRITE_ROWS:
                return ADD;

            case UPDATE_ROWS:
                return UPDATE;

            case DELETE_ROWS:
                return DELETE;

                default:
                    return OTHER;

        }
    }
}
