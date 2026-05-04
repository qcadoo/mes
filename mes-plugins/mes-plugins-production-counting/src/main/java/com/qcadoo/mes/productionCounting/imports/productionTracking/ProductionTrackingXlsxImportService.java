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
package com.qcadoo.mes.productionCounting.imports.productionTracking;

import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.hooks.ProductionTrackingHooks;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ProductionTrackingXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";


    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionTrackingHooks productionTrackingHooks;

    @Override
    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        Entity entity = getDataDefinition(pluginIdentifier, modelName).create();

        setRequiredFields(entity);

        return entity;
    }

    private void setRequiredFields(final Entity productionTracking) {
    }

    @Override
    public void validateEntity(final Entity productionTracking, final DataDefinition productionTrackingDD) {
        validateOrder(productionTracking, productionTrackingDD);
        validateStaff(productionTracking, productionTrackingDD);
        validateDates(productionTracking, productionTrackingDD);
        validateQuantities(productionTracking, productionTrackingDD);
        productionTrackingHooks.copyProducts(productionTracking);
        validateProducts(productionTracking);
    }

    private void validateProducts(Entity productionTracking) {
        List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Optional<Entity> mainOutProduct = trackingOperationProductOutComponents.stream().filter(e -> e.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT).getId().equals(order.getBelongsToField(OrderFields.PRODUCT).getId())).findFirst();
        if (mainOutProduct.isPresent()) {
            Entity outProduct = mainOutProduct.get();
            outProduct.setField(TrackingOperationProductOutComponentFields.USED_QUANTITY, productionTracking.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY));
            outProduct.setField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY, productionTracking.getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY));
            outProduct.setField(TrackingOperationProductOutComponentFields.CAUSE_OF_WASTES, productionTracking.getStringField(TrackingOperationProductOutComponentFields.CAUSE_OF_WASTES));
        }
    }

    private void validateOrder(final Entity productionTracking, final DataDefinition productionTrackingDD) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        if (!OrderStateStringValues.IN_PROGRESS.equals(order.getStringField(OrderFields.STATE))) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.ORDER), "productionCounting.productionTrackingsList.window.mainTab.productionTrackingsList.grid.error.copy");
        }
        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            Entity toc = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                    .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                    .add(SearchRestrictions.isNull(TechnologyOperationComponentFields.PARENT)).setMaxResults(1).uniqueResult();
            productionTracking.setField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, toc);
        }
    }

    private void validateStaff(final Entity productionTracking, final DataDefinition productionTrackingDD) {
        Entity division = productionTracking.getBelongsToField(ProductionTrackingFields.STAFF).getBelongsToField(StaffFields.DIVISION);
        productionTracking.setField(ProductionTrackingFields.DIVISION, division);
    }


    private void validateDates(final Entity productionTracking, final DataDefinition productionTrackingDD) {
        Date timeRangeFrom = productionTracking.getDateField(ProductionTrackingFields.TIME_RANGE_FROM);
        Date timeRangeTo = productionTracking.getDateField(ProductionTrackingFields.TIME_RANGE_TO);

        if (timeRangeFrom.after(timeRangeTo) || timeRangeFrom.equals(timeRangeTo)) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.TIME_RANGE_TO), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }

        productionTracking.setField(ProductionTrackingFields.SHIFT_START_DAY, new DateTime(timeRangeFrom).withTimeAtStartOfDay().toDate());
    }

    private void validateQuantities(Entity productionTracking, DataDefinition productionTrackingDD) {
        if (productionTracking.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY).compareTo(BigDecimal.ZERO) <= 0) {
            productionTracking.addError(productionTrackingDD.getField(TrackingOperationProductOutComponentFields.USED_QUANTITY), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }

        BigDecimal wastesQuantity = productionTracking.getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);
        if (wastesQuantity != null && wastesQuantity.compareTo(BigDecimal.ZERO) < 0) {
            productionTracking.addError(productionTrackingDD.getField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }
    }

}
