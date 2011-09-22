package com.qcadoo.mes.productionCounting.internal.constants;

public interface ProductionCountingConstants {

    public static final String PLUGIN_IDENTIFIER = "productionCounting";

    // MODEL
    public static final String MODEL_PRODUCTION_RECORD = "productionRecord";

    public static final String MODEL_PRODUCTION_BALANCE = "productionBalance";

    public static final String MODEL_PRODUCTION_COUNTING = "productionCounting";

    public static final String MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT = "recordOperationProductInComponent";

    public static final String MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT = "recordOperationProductOutComponent";

    // VIEW
    public static final String VIEW_PRODUCTION_RECORDS_LIST = "productionRecordsList";

    public static final String VIEW_PRODUCTION_RECORD_DETAILS = "productionRecordDetails";

    public static final String VIEW_PRODUCTION_BALANCES_LIST = "productionBalancesList";

    public static final String VIEW_PRODUCTION_BALANCE_DETAILS = "productionBalanceDetails";

    public static final String VIEW_PRODUCTION_COUNTINGS_LIST = "productionCountingsList";

    public static final String VIEW_PRODUCTION_COUNTING_DETAILS = "productionCountingDetails";

    public static final String VIEW_RECORD_OPERATION_PRODUCT_IN_COMPONENT_DETAILS = "recordOperationProductInComponentDetails";

    public static final String VIEW_RECORD_OPERATION_PRODUCT_OUT_COMPONENT_DETAILS = "recordOperationProductOutComponentDetails";
    
    // RECORDING PARAMETERS
    public static final String PARAM_REGISTER_OUT_PRODUCTS = "registerQuantityOutProduct";
    
    public static final String PARAM_REGISTER_IN_PRODUCTS = "registerQuantityInProduct";
    
    public static final String PARAM_REGISTER_TIME = "registerProductionTime";
    
    public final static String PARAM_RECORDING_TYPE_NONE = "01none";

    public final static String PARAM_RECORDING_TYPE_CUMULATED = "02cumulated";

    public final static String PARAM_RECORDING_TYPE_FOREACH = "03forEach";
}
