package org.fh.gae.das.test;

import org.fh.gae.das.template.TemplateHolder;
import org.junit.Test;

public class TemplateHolderTest {
    @Test
    public void testLoad() {
        TemplateHolder holder = new TemplateHolder();
        holder.loadJson("template.json");
        int i = 0;

    }
}
