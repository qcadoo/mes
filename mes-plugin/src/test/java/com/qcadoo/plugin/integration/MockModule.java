package com.qcadoo.plugin.integration;

import com.qcadoo.plugin.internal.api.Module;

public class MockModule implements Module {

    @Override
    public void init() {
        System.out.println("Module init()");
    }

    @Override
    public void enable() {
        System.out.println("Module enable()");
    }

    @Override
    public void disable() {
        System.out.println("Module disable()");
    }

}
