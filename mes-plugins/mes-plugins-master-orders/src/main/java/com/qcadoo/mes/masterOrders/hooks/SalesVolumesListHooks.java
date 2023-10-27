/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.DocumentPositionParametersFieldsMO;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesVolumeFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SalesVolumesListHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillStockFields(view);
        applyFilters(view);
    }

    private void fillStockFields(final ViewDefinitionState view) {
        if (view.isViewAfterRedirect()) {
            List<Entity> salesVolumes = getSalesVolumeDD().find().list().getEntities();

            List<Entity> products = salesVolumes.stream().map(salesVolume -> salesVolume.getBelongsToField(SalesVolumeFields.PRODUCT)).collect(Collectors.toList());
            List<Entity> locations = materialFlowResourcesService.getWarehouseLocationsFromDB();

            Map<Long, Map<Long, BigDecimal>> resourceStocks = materialFlowResourcesService.getQuantitiesForProductsAndLocations(products, locations);

            salesVolumes.forEach(salesVolume -> {
                Entity product = salesVolume.getBelongsToField(SalesVolumeFields.PRODUCT);
                BigDecimal dailySalesVolume = salesVolume.getDecimalField(SalesVolumeFields.DAILY_SALES_VOLUME);

                if (Objects.nonNull(dailySalesVolume) && BigDecimal.ZERO.compareTo(dailySalesVolume) < 0) {
                    BigDecimal currentStock = getCurrentStock(resourceStocks, product.getId());
                    Integer stockForDays = currentStock.divide(dailySalesVolume, 0, RoundingMode.FLOOR).intValue();

                    salesVolume.setField(SalesVolumeFields.CURRENT_STOCK, currentStock);
                    salesVolume.setField(SalesVolumeFields.STOCK_FOR_DAYS, stockForDays);

                    salesVolume.getDataDefinition().fastSave(salesVolume);
                }
            });
        }
    }

    private BigDecimal getCurrentStock(final Map<Long, Map<Long, BigDecimal>> resourceStocks, final Long productId) {
        BigDecimal currentStock = BigDecimal.ZERO;

        for (Map.Entry<Long, Map<Long, BigDecimal>> resourceStock : resourceStocks.entrySet()) {
            currentStock = currentStock.add(BigDecimalUtils.convertNullToZero(resourceStock.getValue().get(productId)),
                    numberService.getMathContext());
        }

        return currentStock;
    }

    public void applyFilters(final ViewDefinitionState view) {
        GridComponent salesVolumesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        CheckBoxComponent isStockForDaysFilterCheckBox = (CheckBoxComponent) view.getComponentByReference("isStockForDaysFilter");

        boolean isStockForDaysFilter = isStockForDaysFilterCheckBox.isChecked();

        Integer runningOutOfStockDays = IntegerUtils.convertNullToZero(getDocumentPositionParameters().getIntegerField(DocumentPositionParametersFieldsMO.RUNNING_OUT_OF_STOCK_DAYS));

        if (isStockForDaysFilter) {
            salesVolumesGrid.setCustomRestriction(searchBuilder -> searchBuilder.add(
                    SearchRestrictions.lt(SalesVolumeFields.STOCK_FOR_DAYS, runningOutOfStockDays)));
        }
    }

    private Entity getDocumentPositionParameters() {
        return getDocumentPositionParametersDD().find().setMaxResults(1).uniqueResult();
    }

    private DataDefinition getDocumentPositionParametersDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS);
    }

    private DataDefinition getSalesVolumeDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_VOLUME);
    }

}
