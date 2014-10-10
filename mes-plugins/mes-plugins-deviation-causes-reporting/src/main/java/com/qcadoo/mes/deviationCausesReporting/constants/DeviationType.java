/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deviationCausesReporting.constants;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

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
