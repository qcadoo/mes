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
package com.qcadoo.mes.productionCounting;

import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionCountingServiceImpl implements ProductionCountingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionCountingServiceImpl.class);

    private static final String L_ORDER = "order";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Override
    public Entity getProductionTracking(final Long productionTrackingId) {
        return getProductionTrackingDD().get(productionTrackingId);
    }

    @Override
    public Entity getProductionBalance(final Long productionBalanceId) {
        return getProductionBalanceDD().get(productionBalanceId);
    }

    @Override
    public Entity getTrackingOperationProductInComponent(final Long trackingOperationProductInComponentId) {
        return getTrackingOperationProductInComponentDD().get(trackingOperationProductInComponentId);
    }

    @Override
    public Entity getTrackingOperationProductOutComponent(final Long trackingOperationProductOutComponentId) {
        return getTrackingOperationProductOutComponentDD().get(trackingOperationProductOutComponentId);
    }

    @Override
    public Entity getStaffWorkTime(final Long staffWorkTimeId) {
        return getStaffWorkTimeDD().get(staffWorkTimeId);
    }

    @Override
    public DataDefinition getProductionTrackingDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_TRACKING);
    }

    @Override
    public DataDefinition getProductionBalanceDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE);
    }

    @Override
    public DataDefinition getTrackingOperationProductInComponentDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT);
    }

    @Override
    public DataDefinition getTrackingOperationProductOutComponentDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT);
    }

    @Override
    public DataDefinition getStaffWorkTimeDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_STAFF_WORK_TIME);
    }

    @Override
    public List<Entity> getProductionTrackingsForOrder(final Entity order) {
        SearchCriteriaBuilder scb = getProductionTrackingDD().find();

        scb.createCriteria(ProductionTrackingFields.ORDER, "ord_alias", JoinType.INNER).add(idEq(order.getId()));
        scb.add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.ACCEPTED));

        return scb.list().getEntities();
    }

    @Override
    public boolean isTypeOfProductionRecordingBasic(final String typeOfProductionRecording) {
        return TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording);
    }

    @Override
    public boolean isTypeOfProductionRecordingForEach(final String typeOfProductionRecording) {
        return TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording);
    }

    @Override
    public boolean isTypeOfProductionRecordingCumulated(final String typeOfProductionRecording) {
        return TypeOfProductionRecording.CUMULATED.getStringValue().equals(typeOfProductionRecording);
    }

    @Override
    public boolean checkIfTypeOfProductionRecordingIsEmptyOrBasic(final String typeOfProductionRecording) {
        return StringUtils.isEmpty(typeOfProductionRecording) || isTypeOfProductionRecordingBasic(typeOfProductionRecording);
    }

    @Override
    public boolean validateOrder(final DataDefinition productionTrackingBalanceDD, final Entity productionTrackingBalance) {
        Entity order = productionTrackingBalance.getBelongsToField(L_ORDER);

        if ((order == null)
                || isTypeOfProductionRecordingBasic(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            productionTrackingBalance.addError(productionTrackingBalanceDD.getField(L_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRecordingType");

            return false;
        }

        List<Entity> productionTrackings = getProductionTrackingsForOrder(order);

        if (productionTrackings.isEmpty()) {
            productionTrackingBalance.addError(productionTrackingBalanceDD.getField(L_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutProductionTrackings");

            return false;
        }

        return true;
    }

    @Override
    public void setComponentsState(final ViewDefinitionState view, final List<String> componentReferenceNames,
            final boolean isEnabled, final boolean requestComponentUpdateState) {
        for (String componentReferenceName : componentReferenceNames) {
            ComponentState componentState = view.getComponentByReference(componentReferenceName);

            if (componentState == null) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(String.format("Cannot find component with reference='%s'", componentReferenceName));
                }
            } else {
                componentState.setEnabled(isEnabled);
            }
        }
    }

    @Override
    public void changeDoneQuantityAndAmountOfProducedQuantityFieldState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        FieldComponent doneQuantityField = (FieldComponent) view.getComponentByReference(OrderFields.DONE_QUANTITY);
        FieldComponent amountOfProductProducedField = (FieldComponent) view
                .getComponentByReference(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED);
        FieldComponent wastesQuantityField = (FieldComponent) view.getComponentByReference(OrderFields.WASTES_QUANTITY);

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
            doneQuantityField.setEnabled(false);
            amountOfProductProducedField.setEnabled(false);
            wastesQuantityField.setEnabled(false);
            return;
        }

        Entity order = orderForm.getEntity().getDataDefinition().get(orderId);

        String state = order.getStringField(OrderFields.STATE);

        if (OrderStateStringValues.IN_PROGRESS.equals(state) || OrderStateStringValues.INTERRUPTED.equals(state)) {
            String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

            if (checkIfTypeOfProductionRecordingIsEmptyOrBasic(typeOfProductionRecording)) {
                doneQuantityField.setEnabled(true);
                wastesQuantityField.setEnabled(true);
                amountOfProductProducedField.setEnabled(false);
            } else {
                doneQuantityField.setEnabled(false);
                wastesQuantityField.setEnabled(false);
                amountOfProductProducedField.setEnabled(false);
            }

        } else {
            doneQuantityField.setEnabled(false);
            wastesQuantityField.setEnabled(false);
            amountOfProductProducedField.setEnabled(false);
        }
    }

    @Override
    public BigDecimal getRegisteredProductValueForOperationProductIn(final Entity operationProduct, final BigDecimal planed) {
        BigDecimal value = null;
        Entity toc = operationProduct.getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);
        Entity product = operationProduct.getBelongsToField(OperationProductInComponentFields.PRODUCT);

        List<Entity> tracings = getProductionTrackingDD().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.ACCEPTED)).list()
                .getEntities();
        for (Entity tracking : tracings) {
            Entity topIN = getTrackingOperationProductInComponentDD().find()
                    .add(SearchRestrictions.belongsTo(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING, tracking))
                    .add(SearchRestrictions.belongsTo(TrackingOperationProductInComponentFields.PRODUCT, product))
                    .setMaxResults(1).uniqueResult();
            if (topIN != null) {
                if (value == null) {
                    value = new BigDecimal(0L);
                }
                BigDecimal usedQuantity = topIN.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
                if (usedQuantity != null) {
                    value = value.add(usedQuantity, numberService.getMathContext());
                }
            }
        }
        if (value != null) {
            value = planed.subtract(value, numberService.getMathContext());
        } else {
            return value;
        }

        if (value.compareTo(new BigDecimal(0L)) < 0) {
            value = new BigDecimal(0L);
        }

        return value;
    }

    @Override
    public BigDecimal getRegisteredProductValueForOperationProductOut(final Entity operationProduct, final BigDecimal planed) {
        BigDecimal value = null;
        Entity toc = operationProduct.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);
        Entity product = operationProduct.getBelongsToField(OperationProductOutComponentFields.PRODUCT);

        List<Entity> tracings = getProductionTrackingDD().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.ACCEPTED)).list()
                .getEntities();
        for (Entity tracking : tracings) {
            Entity topIN = getTrackingOperationProductOutComponentDD().find()
                    .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING, tracking))
                    .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, product))
                    .setMaxResults(1).uniqueResult();
            if (topIN != null) {
                if (value == null) {
                    value = new BigDecimal(0L);
                }
                BigDecimal usedQuantity = topIN.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
                if (usedQuantity != null) {
                    value = value.add(usedQuantity, numberService.getMathContext());
                }
            }
        }
        if (value != null) {
            value = planed.subtract(value, numberService.getMathContext());
        } else {
            return value;
        }

        if (value.compareTo(new BigDecimal(0L)) < 0) {
            value = new BigDecimal(0L);
        }

        return value;
    }
}
