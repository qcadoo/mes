package com.qcadoo.mes.deviationCausesReporting.constants;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.qcadoo.mes.orders.constants.CommonReasonTypeFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.deviationReasonTypes.DeviationModelDescriber;
import com.qcadoo.mes.orders.constants.deviationReasonTypes.OrderDeviationModelDescribers;
import com.qcadoo.mes.productionPerShift.constants.PpsDeviationModelDescribers;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ReasonTypeOfCorrectionPlanFields;

public enum DeviationType {

    START_DATE_DEVIATION(OrderDeviationModelDescribers.START_DATE_DEVIATION) {

        @Override
        public String getCommentPath() {
            return pathThru(getPathToOrder(), OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM);
        }
    },
    FINISH_DATE_DEVIATION(OrderDeviationModelDescribers.FINISH_DATE_DEVIATION) {

        @Override
        public String getCommentPath() {
            return pathThru(getPathToOrder(), OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO);
        }
    },
    EFFECTIVE_START_DATE_DEVIATION(OrderDeviationModelDescribers.EFFECTIVE_START_DATE_DEVIATION) {

        @Override
        public String getCommentPath() {
            return pathThru(getPathToOrder(), OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_START);
        }
    },
    EFFECTIVE_FINISH_DATE_DEVIATION(OrderDeviationModelDescribers.EFFECTIVE_FINISH_DATE_DEVIATION) {

        @Override
        public String getCommentPath() {
            return pathThru(getPathToOrder(), OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_END);
        }
    },
    QUANTITY_DEVIATION(OrderDeviationModelDescribers.QUANTITY_DEVIATION) {

        @Override
        public String getCommentPath() {
            return pathThru(getPathToOrder(), OrderFields.COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY);
        }
    },
    PPS_DEVIATION(PpsDeviationModelDescribers.PPS_DEVIATION) {

        @Override
        public String getPathToOrder() {
            return pathThru(ReasonTypeOfCorrectionPlanFields.PRODUCTION_PER_SHIFT, ProductionPerShiftFields.ORDER);
        }

        @Override
        public String getCommentPath() {
            return pathThru(ReasonTypeOfCorrectionPlanFields.PRODUCTION_PER_SHIFT,
                    ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT);
        }
    };

    private static String pathThru(final String... partials) {
        return StringUtils.join(Arrays.asList(partials), ".");
    }

    private final DeviationModelDescriber modelDescriber;

    private DeviationType(final DeviationModelDescriber modelDescriber) {
        this.modelDescriber = modelDescriber;
    }

    public DeviationModelDescriber getModelDescriber() {
        return modelDescriber;
    }

    public abstract String getCommentPath();

    public String getPathToOrder() {
        return CommonReasonTypeFields.ORDER;
    };
}
