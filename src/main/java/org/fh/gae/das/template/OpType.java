package org.fh.gae.das.template;

import org.fh.gae.das.exception.InvalidDasTemplateException;

public enum OpType {
    ADD,
    UPDATE,
    DELETE;

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
}
