package com.qcadoo.mes;

public class SystemProperties {

    public static enum env {
        AMAZON, LOCAL
    }

    public static env getEnviroment() {
        return env.LOCAL;
    }

}
