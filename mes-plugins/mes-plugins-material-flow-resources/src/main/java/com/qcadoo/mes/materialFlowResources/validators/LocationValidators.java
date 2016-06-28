/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited Project: Qcadoo MES Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.validators;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.LocationType;
import com.qcadoo.mes.materialFlowResources.constants.DocumentPositionParametersItemValues;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class LocationValidators {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean hasAlgorithm(final DataDefinition dataDefinition, final Entity entity) {
        if (LocationType.WAREHOUSE.equals(LocationType.parseString(entity.getStringField(LocationFields.TYPE)))
                && StringUtils.isEmpty(entity.getStringField(LocationFieldsMFR.ALGORITHM))) {
            entity.addError(dataDefinition.getField(LocationFieldsMFR.ALGORITHM), "qcadooView.validate.field.error.missing");
            return false;
        }

        return true;
    }

    public boolean isFieldVisible(final DataDefinition locatoinDD, final Entity location) {
        DataDefinition documentPositionParametersItemDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS_ITEM);
        Map<String, Entity> items = documentPositionParametersItemDD.find().list().getEntities().stream().collect(Collectors.toMap(item -> item.getStringField("name"), item -> item));

        List<String> requiredFields = Arrays.asList(DocumentPositionParametersItemValues.PRICE, DocumentPositionParametersItemValues.BATCH,
                DocumentPositionParametersItemValues.EXPIRATION_DATE, DocumentPositionParametersItemValues.PRODUCTION_DATE);

        for (String name : requiredFields) {
            String camelCaseName = "require" + name.substring(0, 1).toUpperCase() + name.substring(1);
            if (location.getBooleanField(camelCaseName) && !items.get(name).getBooleanField("checked")) {
                String fieldTranslatedName = translationService.translate("materialFlowResources.materialFlowResourcesParameters.documentPositionParameters." + name, LocaleContextHolder.getLocale());
                String errorMessage = translationService.translate("materialFlowResources.error.documentLocationPositionItemIsHidden", LocaleContextHolder.getLocale());
                errorMessage = String.format(errorMessage, fieldTranslatedName);
                location.addError(locatoinDD.getField(camelCaseName), errorMessage);
            }
        }

        return true;
    }
}
