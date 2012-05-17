/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.productionCounting.internal;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_DIVISION;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_SHIFT;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_STAFF;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_WORKSTATION_TYPE;
import static com.qcadoo.mes.orders.constants.OrderStates.ABANDONED;
import static com.qcadoo.mes.orders.constants.OrderStates.ACCEPTED;
import static com.qcadoo.mes.orders.constants.OrderStates.DECLINED;
import static com.qcadoo.mes.orders.constants.OrderStates.PENDING;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.LAST_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.STATE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.BASIC;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionRecordService {

    private static final String L_TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS = "technologyInstanceOperationComponents";

    private static final String L_STATE = "state";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private NumberService numberService;

    public void generateData(final DataDefinition productionRecordDD, final Entity productionRecord) {
        if (productionRecord.getField(NUMBER) == null) {
            productionRecord.setField(NUMBER, numberGeneratorService.generateNumber(
                    ProductionCountingConstants.PLUGIN_IDENTIFIER, productionRecord.getDataDefinition().getName()));
        }
    }

    public boolean checkTypeOfProductionRecording(final DataDefinition productionRecordDD, final Entity productionRecord) {
        final Entity order = productionRecord.getBelongsToField(OrdersConstants.MODEL_ORDER);
        if (order == null) {
            return true;
        }
        final String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);
        return isValidTypeOfProductionRecording(productionRecord, typeOfProductionRecording, productionRecordDD);
    }

    public boolean isValidTypeOfProductionRecording(final Entity productionRecord, final String typeOfProductionRecording,
            final DataDefinition productionRecordDD) {
        boolean validTypeOfRecording = true;
        if (typeOfProductionRecording == null || BASIC.getStringValue().equals(typeOfProductionRecording)) {
            productionRecord.addError(productionRecordDD.getField(OrdersConstants.MODEL_ORDER),
                    "productionCounting.validate.global.error.productionRecord.orderError");
            validTypeOfRecording = false;
        }
        if (BASIC.getStringValue().equals(typeOfProductionRecording)) {
            productionRecord.addError(productionRecordDD.getField(OrdersConstants.MODEL_ORDER),
                    "productionRecord.productionRecord.report.error.orderWithBasicProductionCounting");
            validTypeOfRecording = false;
        }
        return validTypeOfRecording;
    }

    public boolean willOrderAcceptOneMore(final DataDefinition productionRecordDD, final Entity productionRecord) {
        final Entity order = productionRecord.getBelongsToField(OrdersConstants.MODEL_ORDER);
        final Entity operation = productionRecord.getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        final List<Entity> productionCountings = productionRecordDD.find()
                .add(SearchRestrictions.eq(STATE, ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(OrdersConstants.MODEL_ORDER, order))
                .add(SearchRestrictions.belongsTo(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT, operation)).list().getEntities();

        return willOrderAcceptOneMoreValidator(productionCountings, productionRecord, productionRecordDD);
    }

    boolean willOrderAcceptOneMoreValidator(final List<Entity> productionCountings, final Entity productionRecord,
            final DataDefinition dd) {
        for (Entity counting : productionCountings) {
            if (counting.getBooleanField(LAST_RECORD)) {
                if (productionRecord.getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) == null) {
                    productionRecord.addError(dd.getField(OrdersConstants.MODEL_ORDER),
                            "productionCounting.record.messages.error.final");
                } else {
                    productionRecord.addError(dd.getField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT),
                            "productionCounting.record.messages.error.operationFinal");
                }

                return false;
            }
        }

        return true;
    }

    public boolean checkIfOrderIsStarted(final DataDefinition dd, final Entity entity) {
        boolean isStarted = true;
        final String orderState = entity.getBelongsToField(OrdersConstants.MODEL_ORDER).getStringField(L_STATE);
        if (orderState == null || PENDING.getStringValue().equals(orderState) || ACCEPTED.getStringValue().equals(orderState)
                || DECLINED.getStringValue().equals(orderState) || ABANDONED.getStringValue().equals(orderState)) {
            entity.addError(dd.getField(OrdersConstants.MODEL_ORDER),
                    "productionCounting.record.messages.error.orderIsNotStarted");
            isStarted = false;
        }
        return isStarted;
    }

    public void copyProductsFromOrderOperation(final DataDefinition productionRecordDD, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(ORDER);
        Entity technologyInstanceOperationComponent = productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);
        if (typeOfProductionRecording == null) {
            return;
        }
        List<Entity> operationComponents = null;

        Boolean registerInput = order.getBooleanField(REGISTER_QUANTITY_IN_PRODUCT);
        Boolean registerOutput = order.getBooleanField(REGISTER_QUANTITY_OUT_PRODUCT);

        if (!registerInput && !registerOutput) {
            return;
        }

        if (shouldCopy(productionRecord, order, technologyInstanceOperationComponent)) {
            if (CUMULATED.getStringValue().equals(typeOfProductionRecording)) {
                operationComponents = order.getTreeField(L_TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
            } else if (FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
                operationComponents = newArrayList(productionRecord
                        .getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));
            }

            if (registerInput) {
                copyOperationProductComponents(operationComponents, productionRecord,
                        MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT);
            }
            if (registerOutput) {
                copyOperationProductComponents(operationComponents, productionRecord,
                        MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);
            }
        }
    }

    private void copyOperationProductComponents(final List<Entity> orderOperations, final Entity productionRecord,
            final String modelName) {
        if (isOperationListEmpty(orderOperations)) {
            return;
        }

        DataDefinition recordProductDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, modelName);
        Map<Long, Entity> recordProductsMap = newHashMap();
        String productModel = "operationProductOutComponent";
        String recordProductFieldName = RECORD_OPERATION_PRODUCT_OUT_COMPONENTS;

        if (MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT.equals(modelName)) {
            productModel = "operationProductInComponent";
            recordProductFieldName = RECORD_OPERATION_PRODUCT_IN_COMPONENTS;
        }

        for (Entity orderOperation : orderOperations) {
            Entity order = orderOperation.getBelongsToField(OrdersConstants.MODEL_ORDER);

            Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();

            Map<Entity, BigDecimal> productComponentQuantities = productQuantitiesService
                    .getProductComponentQuantities(asList(order));

            Entity technologyInstanceOperationComponent = productionRecord
                    .getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

            for (Entry<Entity, BigDecimal> prodCompQty : productComponentQuantities.entrySet()) {
                Entity product = prodCompQty.getKey().getBelongsToField(BasicConstants.MODEL_PRODUCT);

                if (technologyInstanceOperationComponent != null) {
                    Entity operation = technologyInstanceOperationComponent
                            .getBelongsToField(TechnologiesConstants.MODEL_OPERATION);
                    Entity currentOperation = prodCompQty.getKey().getBelongsToField("operationComponent")
                            .getBelongsToField(TechnologiesConstants.MODEL_OPERATION);
                    if (!operation.getId().equals(currentOperation.getId())) {
                        continue;
                    }
                }

                if (productModel.equals(prodCompQty.getKey().getDataDefinition().getName())) {
                    BigDecimal qty = prodCompQty.getValue();
                    if (productQuantities.get(product) != null) {
                        qty = qty.add(productQuantities.get(product), numberService.getMathContext());
                    }
                    productQuantities.put(product, qty);
                }
            }

            for (Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
                Entity recordProduct = recordProductDD.create();
                recordProduct.setField(BasicConstants.MODEL_PRODUCT, productQuantity.getKey());
                recordProduct.setField(PLANNED_QUANTITY, productQuantity.getValue());
                recordProductsMap.put(productQuantity.getKey().getId(), recordProduct);
            }
        }

        productionRecord.setField(recordProductFieldName, newArrayList(recordProductsMap.values()));
    }

    public boolean checkIfOperationIsSet(final DataDefinition productionRecordDD, final Entity productionRecord) {
        String recordingMode = productionRecord.getBelongsToField(OrdersConstants.MODEL_ORDER).getStringField(
                TYPE_OF_PRODUCTION_RECORDING);
        Object orderOperation = productionRecord.getField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        if (FOR_EACH.getStringValue().equals(recordingMode) && orderOperation == null) {
            productionRecord.addError(productionRecordDD.getField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT),
                    "productionCounting.record.messages.error.operationIsNotSet");
            return false;
        }
        return true;
    }

    public final void changeStateToDefault(final DataDefinition productionRecordDD, final Entity productionRecord) {
        productionRecord.setField(L_STATE, ProductionCountingStates.DRAFT.getStringValue());
    }

    public final void fillShiftAndDivisionField(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        fillShiftAndDivisionField(view);
    }

    public final void fillShiftAndDivisionField(final ViewDefinitionState view) {
        FieldComponent staffLookup = getFieldComponent(view, MODEL_STAFF);
        FieldComponent shiftLookup = getFieldComponent(view, MODEL_SHIFT);
        FieldComponent divisionLookup = getFieldComponent(view, MODEL_DIVISION);

        if (staffLookup.getFieldValue() == null) {
            shiftLookup.setFieldValue(null);
            return;
        }

        Long staffId = Long.valueOf(staffLookup.getFieldValue().toString());
        Entity staff = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF).get(staffId);

        if (staff == null) {
            return;
        }

        Entity shift = staff.getBelongsToField(MODEL_SHIFT);

        if (shift == null) {
            shiftLookup.setFieldValue(null);
        } else {
            shiftLookup.setFieldValue(shift.getId());
        }

        Entity division = staff.getBelongsToField(MODEL_DIVISION);

        if (division == null) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    public final void fillDivisionField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        fillDivisionField(view);
    }

    public final void fillDivisionField(final ViewDefinitionState view) {
        FieldComponent workstationTypeLookup = getFieldComponent(view, MODEL_WORKSTATION_TYPE);
        FieldComponent divisionLookup = getFieldComponent(view, MODEL_DIVISION);

        if (workstationTypeLookup.getFieldValue() == null) {
            divisionLookup.setFieldValue(null);
            return;
        }

        Long workstationTypeId = Long.valueOf(workstationTypeLookup.getFieldValue().toString());
        Entity workstationType = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_WORKSTATION_TYPE).get(workstationTypeId);

        if (workstationType == null) {
            return;
        }

        Entity division = workstationType.getBelongsToField(MODEL_DIVISION);

        if (division == null) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    private boolean shouldCopy(final Entity productionRecord, Entity order, Entity technologyInstanceOperationComponent) {
        return (hasValueChanged(productionRecord, order, ORDER)
                || (technologyInstanceOperationComponent != null && hasValueChanged(productionRecord,
                        technologyInstanceOperationComponent, TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)) || !hasRecordOperationProductComponents(productionRecord));
    }

    private boolean hasRecordOperationProductComponents(final Entity productionRecord) {
        return ((productionRecord.getField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS) != null) && (productionRecord
                .getField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS) != null));
    }

    private boolean hasValueChanged(final Entity productionRecord, final Entity value, final String model) {
        Entity existingProductionRecord = getExistingProductionRecord(productionRecord);
        if (existingProductionRecord == null) {
            return false;
        }
        Entity existingProductionRecordValue = existingProductionRecord.getBelongsToField(model);
        if (existingProductionRecordValue == null) {
            return true;
        }
        return !existingProductionRecordValue.equals(value);
    }

    private Entity getExistingProductionRecord(final Entity productionRecord) {
        if (productionRecord.getId() == null) {
            return null;
        }
        return productionRecord.getDataDefinition().get(productionRecord.getId());
    }

    private static boolean isOperationListEmpty(final List<Entity> orderOperations) {
        return orderOperations == null || orderOperations.isEmpty() || orderOperations.get(0) == null;
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

}
