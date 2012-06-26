package com.qcadoo.mes.productionCounting.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum ProductionRecordState implements StateEnum {

    DRAFT(ProductionRecordStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    ACCEPTED(ProductionRecordStateStringValues.ACCEPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DECLINED.equals(targetState);
        }
    },
    DECLINED(ProductionRecordStateStringValues.DECLINED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    private ProductionRecordState(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public abstract boolean canChangeTo(final StateEnum targetState);

    public static ProductionRecordState parseString(final String string) {
        ProductionRecordState parsedStatus = null;
        for (ProductionRecordState status : ProductionRecordState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse '" + string + "'");
        return parsedStatus;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

}
