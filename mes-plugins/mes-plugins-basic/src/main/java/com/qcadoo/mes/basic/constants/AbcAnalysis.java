package com.qcadoo.mes.basic.constants;

public enum AbcAnalysis {
    GROUP_A("01groupA"), GROUP_B("02groupB"), GROUP_C("03groupC");

    private final String abcAnalysis;

    AbcAnalysis(final String abcAnalysis) {
        this.abcAnalysis = abcAnalysis;
    }

    public String getStringValue() {
        return abcAnalysis;
    }
}
