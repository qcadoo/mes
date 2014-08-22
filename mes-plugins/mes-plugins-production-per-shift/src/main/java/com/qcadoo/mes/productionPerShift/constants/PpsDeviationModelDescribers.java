package com.qcadoo.mes.productionPerShift.constants;

import com.qcadoo.mes.orders.constants.deviationReasonTypes.DeviationModelDescriber;

public final class PpsDeviationModelDescribers {

    private PpsDeviationModelDescribers() {}

    public static final DeviationModelDescriber PPS_DEVIATION = new DeviationModelDescriber(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_REASON_TYPE_OF_CORRECTION_PLAN, ReasonTypeOfCorrectionPlanFields.REASON_TYPE_OF_CORRECTION_PLAN);

}
