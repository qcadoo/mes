/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.validators;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PositionValidators {

    public boolean checkAttributesRequirement(final DataDefinition dataDefinition, final Entity position) {
        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);

        if (document != null) {
            DocumentType documentType = DocumentType.of(document);
            DocumentState documentState = DocumentState.of(document);

            if (documentState == DocumentState.ACCEPTED
                    && (documentType == DocumentType.RECEIPT || documentType == DocumentType.INTERNAL_INBOUND)) {

                Entity product = position.getBelongsToField(PositionFields.PRODUCT);
                boolean batchEvidence = product.getBooleanField(ProductFields.BATCH_EVIDENCE);
                boolean expirationDateEvidence = product.getBooleanField(ProductFields.EXPIRATION_DATE_EVIDENCE);
                boolean result = true;


                if (batchEvidence && position.getField(PositionFields.BATCH) == null) {
                    position.addError(dataDefinition.getField(PositionFields.BATCH), "materialFlow.error.position.batch.required");
                    result = false;
                }

                if (expirationDateEvidence && position.getField(PositionFields.EXPIRATION_DATE) == null) {
                    position.addError(dataDefinition.getField(PositionFields.EXPIRATION_DATE),
                            "materialFlow.error.position.expirationDate.required");
                    result = false;
                }

                return result;
            }
        }

        return true;
    }

    public boolean validateDates(final DataDefinition dataDefinition, final Entity position) {
        Date productionDate = position.getDateField(PositionFields.PRODUCTION_DATE);
        Date expirationDate = position.getDateField(PositionFields.EXPIRATION_DATE);

        if (productionDate != null && expirationDate != null && expirationDate.compareTo(productionDate) < 0) {
            position.addError(dataDefinition.getField(PositionFields.EXPIRATION_DATE),
                    "materialFlow.error.position.expirationDate.lessThenProductionDate");

            return false;
        }

        return true;
    }

}