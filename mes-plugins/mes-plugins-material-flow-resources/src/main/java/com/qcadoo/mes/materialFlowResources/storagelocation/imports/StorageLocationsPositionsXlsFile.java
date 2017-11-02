package com.qcadoo.mes.materialFlowResources.storagelocation.imports;

public enum StorageLocationsPositionsXlsFile {

    A("Storage place", 0, PositionType.TEXT), B("Product", 1, PositionType.TEXT);

    private String columnName;

    private Integer columnNumber;

    private PositionType columnType;

    StorageLocationsPositionsXlsFile(final String columnName, final Integer columnNumber, final PositionType columnType) {
        this.columnName = columnName;
        this.columnNumber = columnNumber;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public PositionType getColumnType() {
        return columnType;
    }

}
