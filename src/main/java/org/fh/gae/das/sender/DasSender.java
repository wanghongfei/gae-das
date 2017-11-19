package org.fh.gae.das.sender;

import org.fh.gae.das.template.DasSerializable;

public interface DasSender {
    void send(DasSerializable data);
}
