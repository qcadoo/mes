package com.qcadoo.mes.deviationCausesReporting.constants;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.deviationReasonTypes.DeviationModelDescriber;
import com.qcadoo.mes.orders.constants.deviationReasonTypes.OrderDeviationModelDescribers;

public enum DeviationType {

    START_DATE_DEVIATION(OrderDeviationModelDescribers.START_DATE_DEVIATION, OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM) {
    },
    FINISH_DATE_DEVIATION(OrderDeviationModelDescribers.FINISH_DATE_DEVIATION, OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO) {
    },
    EFFECTIVE_START_DATE_DEVIATION(OrderDeviationModelDescribers.EFFECTIVE_START_DATE_DEVIATION,
            OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_START) {
    },
    EFFECTIVE_FINISH_DATE_DEVIATION(OrderDeviationModelDescribers.EFFECTIVE_FINISH_DATE_DEVIATION,
            OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_END) {
    },
    QUANTITY_DEVIATION(OrderDeviationModelDescribers.QUANTITY_DEVIATION, OrderFields.COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY) {
    };

    private final DeviationModelDescriber modelDescriber;

    private final String commentInOrderFieldName;

    private DeviationType(final DeviationModelDescriber modelDescriber, final String commentInOrderFieldName) {
        this.modelDescriber = modelDescriber;
        this.commentInOrderFieldName = commentInOrderFieldName;
    }

    public DeviationModelDescriber getModelDescriber() {
        return modelDescriber;
    }

    public String getCommentInOrderFieldName() {
        return commentInOrderFieldName;
    }

}
