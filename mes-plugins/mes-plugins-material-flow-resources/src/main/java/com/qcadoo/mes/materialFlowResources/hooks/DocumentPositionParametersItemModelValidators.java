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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.DocumentPositionParametersItemValues;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

@Service
public class DocumentPositionParametersItemModelValidators {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public boolean validate(final DataDefinition itemDD, final Entity item) {
        if (!item.getBooleanField("checked")) {
            String name = item.getStringField("name");

            switch (name) {
                case DocumentPositionParametersItemValues.PRICE:
                case DocumentPositionParametersItemValues.BATCH:
                case DocumentPositionParametersItemValues.PRODUCTION_DATE:
                case DocumentPositionParametersItemValues.EXPIRATION_DATE:
                    DataDefinition locationDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
                    String camelCaseName = "require" + name.substring(0, 1).toUpperCase() + name.substring(1);
                    SearchResult result = locationDD.find().add(SearchRestrictions.eq(camelCaseName, true)).setMaxResults(1).list();

                    if (result.getTotalNumberOfEntities() > 0) {
                        String locationName = result.getEntities().get(0).getStringField(LocationFields.NAME);
                        String fieldTranslatedName = translationService.translate("materialFlowResources.materialFlowResourcesParameters.documentPositionParameters." + name, LocaleContextHolder.getLocale());
                        String errorMessage = translationService.translate("materialFlowResources.error.documentLocationPositionItemCantBeHidden", LocaleContextHolder.getLocale());
                        errorMessage = String.format(errorMessage, fieldTranslatedName, locationName);
                        item.addError(itemDD.getField("checked"), errorMessage);
                        
                        return false;
                    }
            }
        }

        return true;
    }

}
