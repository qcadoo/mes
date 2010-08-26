package com.qcadoo.mes.plugins.products.utils;

import org.springframework.context.ApplicationContext;

public class AppContext {

    private static ApplicationContext ctx;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }
}