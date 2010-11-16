/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.search;

public enum RestrictionOperator {

    EQ("="), GE(">="), GT(">"), LE("<="), LT("<"), NE("<>");

    private String value;

    private RestrictionOperator(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
