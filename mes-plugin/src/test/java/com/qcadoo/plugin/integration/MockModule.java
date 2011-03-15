package com.qcadoo.plugin.integration;

import com.qcadoo.plugin.internal.api.Module;

public class MockModule implements Module {

    public static int initCallCount = 0;

    public static int enableCallCount = 0;

    public static int disableCallCount = 0;

    @Override
    public void init() {
        initCallCount++;
    }

    @Override
    public void enable() {
        enableCallCount++;
    }

    @Override
    public void disable() {
        disableCallCount++;
    }

    public static int getInitCallCount() {
        return initCallCount;
    }

    public static int getEnableCallCount() {
        return enableCallCount;
    }

    public static int getDisableCallCount() {
        return disableCallCount;
    }

}
