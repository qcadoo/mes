/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.productionPerShift.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ReasonTypeOfCorrectionPlanFields;
import com.qcadoo.mes.productionPerShift.domain.PpsCorrectionReason;
import com.qcadoo.mes.productionPerShift.domain.ProductionPerShiftId;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class PpsCorrectionReasonAppender {

    private final DataDefinitionService dataDefinitionService;

    @Autowired
    public PpsCorrectionReasonAppender(final DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    public Either<String, Entity> append(final ProductionPerShiftId ppsId, final PpsCorrectionReason reason) {
        if (ppsId == null) {
            return Either.left("Missing pps id!");
        }
        if (reason == null || StringUtils.isBlank(reason.get())) {
            return Either.left("Missing or blank reason type value!");
        }
        return trySave(createCorrectionReason(ppsId, reason));
    }

    private Entity createCorrectionReason(final ProductionPerShiftId ppsId, final PpsCorrectionReason reason) {
        Entity correctionReason = getPpsCorrectionReasonDD().create();
        correctionReason.setField(ReasonTypeOfCorrectionPlanFields.PRODUCTION_PER_SHIFT, ppsId.get());
        correctionReason.setField(ReasonTypeOfCorrectionPlanFields.REASON_TYPE_OF_CORRECTION_PLAN, reason.get());
        return correctionReason;
    }

    private Either<String, Entity> trySave(final Entity entity) {
        DataDefinition dataDef = entity.getDataDefinition();
        Entity savedEntity = dataDef.save(entity);
        if (savedEntity.isValid()) {
            return Either.right(savedEntity);
        }
        return Either.left(String.format("Cannot save %s.%s because of validation errors", dataDef.getPluginIdentifier(),
                dataDef.getName()));
    }

    private DataDefinition getPpsCorrectionReasonDD() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_REASON_TYPE_OF_CORRECTION_PLAN);
    }

}
