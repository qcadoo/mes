package com.qcadoo.mes.orders.constants.deviationReasonTypes;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ReasonTypeCorrectionDateFromFields;
import com.qcadoo.mes.orders.constants.ReasonTypeCorrectionDateToFields;
import com.qcadoo.mes.orders.constants.ReasonTypeDeviationEffectiveEndFields;
import com.qcadoo.mes.orders.constants.ReasonTypeDeviationEffectiveStartFields;
import com.qcadoo.mes.orders.constants.TypeOfCorrectionCausesFields;

public class OrderDeviationModelDescribers {

    public static final DeviationModelDescriber START_DATE_DEVIATION = new DeviationModelDescriber(
            OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_REASON_TYPE_CORRECTION_DATE_FROM,
            ReasonTypeCorrectionDateFromFields.REASON_TYPE_OF_CHANGING_ORDER_STATE);

    public static final DeviationModelDescriber FINISH_DATE_DEVIATION = new DeviationModelDescriber(
            OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_REASON_TYPE_CORRECTION_DATE_TO,
            ReasonTypeCorrectionDateToFields.REASON_TYPE_OF_CHANGING_ORDER_STATE);

    public static final DeviationModelDescriber EFFECTIVE_START_DATE_DEVIATION = new DeviationModelDescriber(
            OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_REASON_TYPE_DEVIATION_EFFECTIVE_START,
            ReasonTypeDeviationEffectiveStartFields.REASON_TYPE_OF_CHANGING_ORDER_STATE);

    public static final DeviationModelDescriber EFFECTIVE_FINISH_DATE_DEVIATION = new DeviationModelDescriber(
            OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_REASON_TYPE_DEVIATION_EFFECTIVE_END,
            ReasonTypeDeviationEffectiveEndFields.REASON_TYPE_OF_CHANGING_ORDER_STATE);

    public static final DeviationModelDescriber QUANTITY_DEVIATION = new DeviationModelDescriber(
            OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_TYPE_OF_CORRECTION_CAUSES,
            TypeOfCorrectionCausesFields.REASON_TYPE);

}
