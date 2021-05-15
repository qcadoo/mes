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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.service.DocumentService;
import com.qcadoo.mes.materialFlowResources.service.DocumentStateChangeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DocumentModelHooks {

    private static final String TYPE_TRANSLATION_PREFIX = "materialFlowResources.generateNumber.type.";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DocumentStateChangeService documentStateChangeService;

    @Autowired
    private DocumentService documentService;

    public void onCreate(final DataDefinition documentDD, final Entity document) {
        setInitialDocumentNumber(document);
        setInitialDocumentInBuffer(document);
        setInitialDocumentAcceptationInProgress(document);
    }

    private void setInitialDocumentNumber(final Entity document) {
        String translatedType = getTranslatedType(document);

        document.setField(DocumentFields.NUMBER, translatedType);
    }

    private void setInitialDocumentInBuffer(final Entity document) {
        if (document.getField(DocumentFields.IN_BUFFER) == null) {
            document.setField(DocumentFields.IN_BUFFER, false);
        }
    }

    private void setInitialDocumentAcceptationInProgress(final Entity document) {
        if (document.getField(DocumentFields.ACCEPTATION_IN_PROGRESS) == null) {
            document.setField(DocumentFields.ACCEPTATION_IN_PROGRESS, false);
        }
    }

    public void onCopy(final DataDefinition documentDD, final Entity document) {
        String translatedType = getTranslatedType(document);

        document.setField(DocumentFields.NUMBER, translatedType);
        document.setField(DocumentFields.NAME, null);
        document.setField(DocumentFields.TIME, new Date());
    }

    public void onSave(final DataDefinition documentDD, final Entity document) {
        if (document.getBooleanField(DocumentFields.IN_BUFFER) && checkIfLocationsChange(document)) {
            cleanPositionsResource(document);
        }
        if (document.getId() == null) {
            documentStateChangeService.buildInitialStateChange(document);
        }
        if (DocumentState.ACCEPTED.getStringValue().equals(document.getStringField(DocumentFields.STATE))) {
            documentStateChangeService.buildSuccessfulStateChange(document);
        }
    }

    private void cleanPositionsResource(final Entity document) {
        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);

        positions.forEach(pos -> {
            pos.setField(PositionFields.RESOURCE, null);
            pos.getDataDefinition().save(pos);
        });
    }

    private boolean checkIfLocationsChange(final Entity document) {
        if (document.getId() == null) {
            return false;
        }

        Entity documentDB = document.getDataDefinition().get(document.getId());
        String documentType = document.getStringField(DocumentFields.TYPE);

        if (DocumentType.RECEIPT.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_INBOUND.getStringValue().equals(documentType)) {
            return checkWarehouse(document, documentDB, false, true);
        } else if (DocumentType.TRANSFER.getStringValue().equals(documentType)) {
            return checkWarehouse(document, documentDB, true, true);
        } else if (DocumentType.RELEASE.getStringValue().equals(documentType)
                || DocumentType.INTERNAL_OUTBOUND.getStringValue().equals(documentType)) {
            return checkWarehouse(document, documentDB, true, false);
        }

        return false;
    }

    private boolean checkWarehouse(final Entity document, final Entity documentDB, boolean from, boolean to) {
        boolean changed = false;

        if (from) {
            if (!documentDB.getBelongsToField(DocumentFields.LOCATION_FROM).getId()
                    .equals(document.getBelongsToField(DocumentFields.LOCATION_FROM).getId())) {
                changed = true;
            }
        } else if (to) {
            if (!documentDB.getBelongsToField(DocumentFields.LOCATION_TO).getId()
                    .equals(document.getBelongsToField(DocumentFields.LOCATION_TO).getId())) {
                changed = true;
            }
        }

        return changed;
    }

    private String getTranslatedType(final Entity document) {
        /**
         * number is generated in database trigger from translated type *
         */
        return translationService.translate(TYPE_TRANSLATION_PREFIX + document.getStringField(DocumentFields.TYPE),
                LocaleContextHolder.getLocale());
    }

    public boolean onDelete(final DataDefinition documentDD, final Entity document) {
        documentService.updateOrdersGroupIssuedMaterials(document, true);

        return true;
    }

}
