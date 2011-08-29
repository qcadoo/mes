package com.qcadoo.mes.costNormsForProduct.constants;


public enum ProductsCostCalculationConstants {
    AVERAGE("averageCost"),
    LASTPURCHASE("lastPurchaseCost"),
    NOMINAL("nominalCost");
    
    private final String strValue;

    private ProductsCostCalculationConstants(final String strValue) {
        this.strValue = strValue;
    }
    
    public String getStrValue() {
        return strValue;
    }
    
}
