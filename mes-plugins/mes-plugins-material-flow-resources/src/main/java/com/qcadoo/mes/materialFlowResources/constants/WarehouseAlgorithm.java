package com.qcadoo.mes.materialFlowResources.constants;

public enum WarehouseAlgorithm {
    FIFO("01fifo"), LIFO("02lifo"), FEFO("03fefo"), LEFO("04lefo"), MANUAL("05manual");

    private final String value;

    private WarehouseAlgorithm(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static WarehouseAlgorithm parseString(final String type) {
        if (LIFO.getStringValue().equalsIgnoreCase(type)) {
            return LIFO;
        } else if (FEFO.getStringValue().equalsIgnoreCase(type)) {
            return FEFO;
        } else if (LEFO.getStringValue().equalsIgnoreCase(type)) {
            return LEFO;
        } else if (MANUAL.getStringValue().equalsIgnoreCase(type)) {
            return MANUAL;
        } else {
            return FIFO;
        }
    }
}
