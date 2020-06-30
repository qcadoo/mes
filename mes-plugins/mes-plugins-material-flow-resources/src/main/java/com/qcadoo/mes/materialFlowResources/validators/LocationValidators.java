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

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

@Service
public class LocationValidators {

    private static final List<String> DOCUMENT_TYPES = Lists.newArrayList(DocumentType.INTERNAL_OUTBOUND.getStringValue(),
            DocumentType.RELEASE.getStringValue(), DocumentType.TRANSFER.getStringValue());

    private static final String DRAFT_MAKES_RESERVATION = "draftMakesReservation";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean hasAlgorithm(final DataDefinition dataDefinition, final Entity entity) {
        if (StringUtils.isEmpty(entity.getStringField(LocationFieldsMFR.ALGORITHM))) {
            entity.addError(dataDefinition.getField(LocationFieldsMFR.ALGORITHM), "qcadooView.validate.field.error.missing");
            return false;
        }

        return true;
    }

    public boolean isFieldVisible(final DataDefinition locatoinDD, final Entity location) {
        DataDefinition documentPositionParametersItemDD = dataDefinitionService.get(
                MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS_ITEM);
        Map<String, Entity> items = documentPositionParametersItemDD.find().list().getEntities().stream()
                .collect(Collectors.toMap(item -> item.getStringField("name"), item -> item));

        List<String> requiredFields = Arrays.asList(DocumentPositionParametersItemValues.PRICE,
                DocumentPositionParametersItemValues.BATCH, DocumentPositionParametersItemValues.EXPIRATION_DATE,
                DocumentPositionParametersItemValues.PRODUCTION_DATE);

        for (String name : requiredFields) {
            String camelCaseName = "require" + name.substring(0, 1).toUpperCase() + name.substring(1);
            if (location.getBooleanField(camelCaseName) && !items.get(name).getBooleanField("checked")) {
                String fieldTranslatedName = translationService.translate(
                        "materialFlowResources.materialFlowResourcesParameters.documentPositionParameters." + name,
                        LocaleContextHolder.getLocale());
                String errorMessage = translationService.translate(
                        "materialFlowResources.error.documentLocationPositionItemIsHidden", LocaleContextHolder.getLocale());
                errorMessage = String.format(errorMessage, fieldTranslatedName);
                location.addError(locatoinDD.getField(camelCaseName), errorMessage);
            }
        }

        return true;
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        if (draftMakesReservationChanged(dataDefinition, entity) && !noDraftDocumentExists(entity)) {
            entity.addError(dataDefinition.getField(DRAFT_MAKES_RESERVATION),
                    "materialFlowResources.materialFlowResourcesLocation.error.draftDocumentsExist");
            return false;
        }
        if (entity.getBooleanField(DRAFT_MAKES_RESERVATION)) {
            return true;
        }
        boolean isValid = noReservationExists(entity);
        if (!isValid) {
            entity.addError(dataDefinition.getField(DRAFT_MAKES_RESERVATION),
                    "materialFlowResources.materialFlowResourcesLocation.error.reservationsExist");
        }
        return isValid;
    }

    private boolean draftMakesReservationChanged(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getId() == null) {
            return false;
        }
        Entity entityFromDb = dataDefinition.get(entity.getId());
        return entityFromDb.getBooleanField(DRAFT_MAKES_RESERVATION) != entity.getBooleanField(DRAFT_MAKES_RESERVATION);
    }

    public boolean noReservationExists(Entity entity) {
        if (entity.getId() == null) {
            return true;
        }

        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION).find();
        scb.add(SearchRestrictions.isNotNull(ReservationFields.POSITION));
        scb.add(SearchRestrictions.belongsTo(ReservationFields.LOCATION, entity));
        scb.setProjection(alias(rowCount(), "cnt"));
        scb.addOrder(asc("cnt"));

        Entity countProjection = scb.setMaxResults(1).uniqueResult();

        return ((Long) countProjection.getField("cnt")).compareTo(0L) == 0;
    }

    public boolean noDraftDocumentExists(Entity entity) {
        if (entity.getId() == null) {
            return true;
        }

        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT).find();
        scb.add(SearchRestrictions.eq(DocumentFields.STATE, DocumentState.DRAFT.getStringValue()));
        scb.add(SearchRestrictions.in(DocumentFields.TYPE, DOCUMENT_TYPES));
        scb.add(SearchRestrictions.belongsTo(DocumentFields.LOCATION_FROM, entity));
        scb.setProjection(alias(rowCount(), "cnt"));
        scb.addOrder(asc("cnt"));

        Entity countProjection = scb.setMaxResults(1).uniqueResult();

        return ((Long) countProjection.getField("cnt")).compareTo(0L) == 0;
    }
}
