package com.qcadoo.mes.orders.deviations.constants;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ReasonTypeCorrectionDateFromFields;
import com.qcadoo.mes.orders.constants.ReasonTypeCorrectionDateToFields;
import com.qcadoo.mes.orders.constants.ReasonTypeDeviationEffectiveEndFields;
import com.qcadoo.mes.orders.constants.ReasonTypeDeviationEffectiveStartFields;
import com.qcadoo.mes.orders.constants.TypeOfCorrectionCausesFields;

public enum DeviationType {

    START_DATE_DEVIATION(OrdersConstants.MODEL_REASON_TYPE_CORRECTION_DATE_FROM,
            OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM,
            ReasonTypeCorrectionDateFromFields.REASON_TYPE_OF_CHANGING_ORDER_STATE) {
    },
    FINISH_DATE_DEVIATION(OrdersConstants.MODEL_REASON_TYPE_CORRECTION_DATE_TO,
            OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO,
            ReasonTypeCorrectionDateToFields.REASON_TYPE_OF_CHANGING_ORDER_STATE) {
    },
    EFFECTIVE_START_DATE_DEVIATION(OrdersConstants.MODEL_REASON_TYPE_DEVIATION_EFFECTIVE_START,
            OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_START,
            ReasonTypeDeviationEffectiveStartFields.REASON_TYPE_OF_CHANGING_ORDER_STATE) {
    },
    EFFECTIVE_FINISH_DATE_DEVIATION(OrdersConstants.MODEL_REASON_TYPE_DEVIATION_EFFECTIVE_END,
            OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_END,
            ReasonTypeDeviationEffectiveEndFields.REASON_TYPE_OF_CHANGING_ORDER_STATE) {
    },
    QUANTITY_DEVIATION(OrdersConstants.MODEL_TYPE_OF_CORRECTION_CAUSES, OrderFields.COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY,
            TypeOfCorrectionCausesFields.REASON_TYPE) {
    };

    private final String reasonModelName;

    private final String commentInOrderFieldName;

    private final String reasonTypeInReasonModelFieldName;

    private DeviationType(final String reasonModelName, final String commentInOrderFieldName, final String reasonTypeFieldName) {
        this.reasonModelName = reasonModelName;
        this.commentInOrderFieldName = commentInOrderFieldName;
        this.reasonTypeInReasonModelFieldName = reasonTypeFieldName;
    }

    public String getReasonModelName() {
        return reasonModelName;
    }

    public String getCommentInOrderFieldName() {
        return commentInOrderFieldName;
    }

    public String getReasonTypeInReasonModelFieldName() {
        return reasonTypeInReasonModelFieldName;
    }
}
