package com.qcadoo.mes.masterOrders.hooks;

public enum MasterOrderPositionStatus {

    NEW("01new", "nowa"), ORDERED("02ordered", "zlecona"), RELEASED("03released", "wydana");

    private final String technicalCode;
    private final String text;

    MasterOrderPositionStatus(final String technicalCode, final String text) {
        this.technicalCode = technicalCode;
        this.text = text;

    }

    public String getStringValue() {
        return technicalCode;
    }

    public String getText() {
        return text;
    }

    public static MasterOrderPositionStatus parseString(final String val) {
        for (MasterOrderPositionStatus status : values()) {
            if (status.getStringValue().equals(val)) {
                return status;
            }
        }

        throw new IllegalStateException("Unsupported MasterOrderPositionStatus: " + val);
    }

}
