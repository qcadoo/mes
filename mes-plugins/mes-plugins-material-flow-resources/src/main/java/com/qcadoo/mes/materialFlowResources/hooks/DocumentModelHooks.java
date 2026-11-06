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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.service.DocumentService;
import com.qcadoo.mes.materialFlowResources.service.DocumentStateChangeService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DocumentModelHooks {

    private static final String TYPE_TRANSLATION_PREFIX = "materialFlowResources.generateNumber.type.";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DocumentStateChangeService documentStateChangeService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCreate(final DataDefinition documentDD, final Entity document) {
        setInitialDocumentNumber(document);
        setInitialDocumentAcceptationInProgress(document);
    }

    private void setInitialDocumentNumber(final Entity document) {
        String translatedType = getTranslatedType(document);

        document.setField(DocumentFields.NUMBER, translatedType);
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
        document.setField(DocumentFields.WMS, false);
        document.setCopied(true);
    }

    public void onSave(final DataDefinition documentDD, final Entity document) {
        if (document.getId() == null) {
            documentStateChangeService.buildInitialStateChange(document);
        }
        if (DocumentState.ACCEPTED.getStringValue().equals(document.getStringField(DocumentFields.STATE))) {
            documentStateChangeService.buildSuccessfulStateChange(document);
        }
        if (document.isCopied()) {
            validateAvailableQuantities(document);
        }
    }

    public void validateAvailableQuantities(final Entity document) {
        String type = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.isOutbound(type)) {
            Entity location = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            boolean enabled = location.getBooleanField(LocationFieldsMFR.DRAFT_MAKES_RESERVATION);
            if (enabled) {
                List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
                Map<Long, BigDecimal> groupedPositions = groupProductsInPositions(positions);

                for (Entity position : positions) {
                    Entity product = position.getBelongsToField(PositionFields.PRODUCT);
                    BigDecimal availableQuantity = getAvailableQuantityForProductAndLocation(
                            position.getBelongsToField(PositionFields.PRODUCT), location);
                    if (groupedPositions.get(product.getId()).compareTo(availableQuantity) > 0) {
                        document.addGlobalError("materialFlowResources.stocktaking.document.quantity.notEnoughResources", false, product.getStringField(ProductFields.NUMBER));
                    }
                }
            }
        }
    }


    private Map<Long, BigDecimal> groupProductsInPositions(final List<Entity> positions) {
        Map<Long, BigDecimal> groupedPositions = Maps.newHashMap();

        for (Entity position : positions) {
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);
            BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);

            if (groupedPositions.containsKey(product.getId())) {
                groupedPositions.put(product.getId(), groupedPositions.get(product.getId()).add(quantity));
            } else {
                groupedPositions.put(product.getId(), quantity);
            }
        }

        return groupedPositions;
    }

    private BigDecimal getAvailableQuantityForProductAndLocation(Entity product, Entity location) {
        Entity resourceStockDto = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO)
                .find().add(SearchRestrictions.eq(ResourceStockDtoFields.PRODUCT_ID, product.getId().intValue()))
                .add(SearchRestrictions.eq(ResourceStockDtoFields.LOCATION_ID, location.getId().intValue())).setMaxResults(1)
                .uniqueResult();
        if (Objects.isNull(resourceStockDto)) {
            return BigDecimal.ZERO;
        }
        return BigDecimalUtils.convertNullToZero(resourceStockDto.getDecimalField(ResourceStockDtoFields.AVAILABLE_QUANTITY));
    }

    private String getTranslatedType(final Entity document) {
        /**
         * number is generated in database trigger from translated type *
         */
        return translationService.translate(TYPE_TRANSLATION_PREFIX + document.getStringField(DocumentFields.TYPE),
                LocaleContextHolder.getLocale());
    }

    public boolean onDelete(final DataDefinition documentDD, final Entity document) {
        documentService.updateOrdersGroupIssuedMaterials(document.getBelongsToField(OrdersGroupIssuedMaterialFields.ORDERS_GROUP),
                document.getId());

        return true;
    }

}
