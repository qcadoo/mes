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
package com.qcadoo.mes.materialFlowResources.hooks;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.service.ReservationsService;
import com.qcadoo.mes.materialFlowResources.validators.DocumentValidators;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DocumentModelHooks {

    private static final String TYPE_TRANSLATION_PREFIX = "materialFlowResources.generateNumber.type.";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DocumentValidators documentValidators;

    @Autowired
    private ReservationsService reservationsService;

    public void onCreate(final DataDefinition documentDD, final Entity document) {
        String translatedType = getTranslatedType(document);

        document.setField(DocumentFields.NUMBER, translatedType);
        if (reservationsService.reservationsEnabledForDocumentPositions(document)) {
            documentValidators.validateAvailableQuantities(document);
        }
    }

    public void onCopy(final DataDefinition documentDD, final Entity document) {
        String translatedType = getTranslatedType(document);

        document.setField(DocumentFields.NUMBER, translatedType);
        document.setField(DocumentFields.NAME, null);
        document.setField(DocumentFields.TIME, new Date());
        // documentValidators.validateAvailableQuantities(document);
    }

    public void onSave(final DataDefinition documentDD, final Entity document) {
        if (reservationsService.reservationsEnabledForDocumentPositions(document)) {
            documentValidators.validateAvailableQuantities(document);
        }
    }

    private String getTranslatedType(Entity document) {
        /**
         * number is generated in database trigger from translated type *
         */
        return translationService.translate(TYPE_TRANSLATION_PREFIX + document.getStringField(DocumentFields.TYPE),
                LocaleContextHolder.getLocale());
    }

}
