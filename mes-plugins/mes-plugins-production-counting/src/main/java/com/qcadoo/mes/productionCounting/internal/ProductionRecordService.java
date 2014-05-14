/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_DIVISION;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_SHIFT;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_STAFF;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_WORKSTATION_TYPE;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.PRODUCT;
import static com.qcadoo.mes.orders.states.constants.OrderState.ACCEPTED;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.LAST_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.STATE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.BASIC;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;
import static com.qcadoo.mes.technologies.constants.OperationProductInComponentFields.OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingFields;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionRecordService {

    private static final Set<String> ORDER_STARTED_STATES = Sets.newHashSet(OrderState.IN_PROGRESS.getStringValue(),
            OrderState.COMPLETED.getStringValue(), OrderState.INTERRUPTED.getStringValue());

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ParameterService parameterService;

    public void generateData(final DataDefinition productionRecordDD, final Entity productionRecord) {
        if (productionRecord.getField(NUMBER) == null) {
            Entity parameter = parameterService.getParameter();
            if (parameter.getBooleanField(ParameterFieldsPC.GENERATE_PRODUCTION_RECORD_NUMBER_FROM_ORDER_NUMBER)) {
                String[] orderNumberSplited = productionRecord.getBelongsToField(ProductionCountingFields.ORDER)
                        .getStringField(OrderFields.NUMBER).split("-");
                if (orderNumberSplited.length > 1) {
                    String productionRecordNumber = getProductionRecordNumber(orderNumberSplited);
                    productionRecord.setField(NUMBER, productionRecordNumber);
                } else {
                    productionRecord.setField(NUMBER, numberGeneratorService.generateNumber(
                            ProductionCountingConstants.PLUGIN_IDENTIFIER, productionRecord.getDataDefinition().getName()));
                }
            } else {
                productionRecord.setField(NUMBER, numberGeneratorService.generateNumber(
                        ProductionCountingConstants.PLUGIN_IDENTIFIER, productionRecord.getDataDefinition().getName()));
            }
        }
    }

    private String getProductionRecordNumber(final String[] orderNumberSplited) {
        StringBuffer number = new StringBuffer();
        for (int i = 0; i < orderNumberSplited.length; i++) {
            if (i > 0) {
                number.append(orderNumberSplited[i]);
                if (i != orderNumberSplited.length - 1) {
                    number.append("-");
                }
            }
        }
        return StringUtils.strip(number.toString());
    }

    public boolean checkTimeRange(final DataDefinition productionRecordDD, final Entity productionRecord) {
        Date timeRangeFrom = productionRecord.getDateField(ProductionRecordFields.TIME_RANGE_FROM);
        Date timeRangeTo = productionRecord.getDateField(ProductionRecordFields.TIME_RANGE_TO);

        if (timeRangeFrom == null || timeRangeTo == null || timeRangeTo.after(timeRangeFrom)) {
            return true;
        }
        productionRecord.addError(productionRecordDD.getField(ProductionRecordFields.TIME_RANGE_TO),
                "productionCounting.productionRecord.productionRecordError.timeRangeToBeforetumeRangeFrom");
        return false;
    }

    public boolean checkTypeOfProductionRecording(final DataDefinition productionRecordDD, final Entity productionRecord) {
        final Entity order = productionRecord.getBelongsToField(ORDER);
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
            productionRecord.addError(productionRecordDD.getField(ORDER),
                    "productionCounting.validate.global.error.productionRecord.orderError");
            validTypeOfRecording = false;
        }
        if (BASIC.getStringValue().equals(typeOfProductionRecording)) {
            productionRecord.addError(productionRecordDD.getField(ORDER),
                    "productionCounting.productionRecord.report.error.orderWithBasicProductionCounting");
            validTypeOfRecording = false;
        }
        return validTypeOfRecording;
    }

    public boolean willOrderAcceptOneMore(final DataDefinition productionRecordDD, final Entity productionRecord) {
        final Entity order = productionRecord.getBelongsToField(ORDER);
        final Entity operation = productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        final List<Entity> productionCountings = productionRecordDD.find()
                .add(SearchRestrictions.eq(STATE, ACCEPTED.getStringValue())).add(SearchRestrictions.belongsTo(ORDER, order))
                .add(SearchRestrictions.belongsTo(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT, operation)).list().getEntities();

        return willOrderAcceptOneMoreValidator(productionCountings, productionRecord, productionRecordDD);
    }

    boolean willOrderAcceptOneMoreValidator(final List<Entity> productionCountings, final Entity productionRecord,
            final DataDefinition dd) {
        for (Entity counting : productionCountings) {
            if (counting.getBooleanField(LAST_RECORD)) {
                if (productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) == null) {
                    productionRecord.addError(dd.getField(ORDER), "productionCounting.productionRecord.messages.error.final");
                } else {
                    productionRecord.addError(dd.getField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT),
                            "productionCounting.productionRecord.messages.error.operationFinal");
                }

                return false;
            }
        }

        return true;
    }

    public boolean checkIfOrderIsStarted(final DataDefinition dd, final Entity entity) {
        boolean isStarted = true;
        final String orderState = entity.getBelongsToField(ORDER).getStringField(OrderFields.STATE);
        if (!isOrderStarted(orderState)) {
            entity.addError(dd.getField(ORDER), "productionCounting.productionRecord.messages.error.orderIsNotStarted");
            isStarted = false;
        }
        return isStarted;
    }

    public boolean isOrderStarted(final String orderState) {
        return ORDER_STARTED_STATES.contains(orderState);
    }

    public void copyProductsFromOrderOperation(final DataDefinition productionRecordDD, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(ORDER);
        Entity technologyInstanceOperationComponent = productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);
        if (typeOfProductionRecording == null) {
            return;
        }

        boolean registerInput = order.getBooleanField(REGISTER_QUANTITY_IN_PRODUCT);
        boolean registerOutput = order.getBooleanField(REGISTER_QUANTITY_OUT_PRODUCT);

        if (!registerInput && !registerOutput) {
            return;
        }

        if (shouldCopy(productionRecord, order, technologyInstanceOperationComponent)) {
            if (registerInput) {
                copyOperationProductComponents(productionRecord, order, technologyInstanceOperationComponent,
                        MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT);
            }
            if (registerOutput) {
                copyOperationProductComponents(productionRecord, order, technologyInstanceOperationComponent,
                        MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);
            }
        }
    }

    private void copyOperationProductComponents(final Entity productionRecord, final Entity order,
            final Entity technologyInstanceOperationComponent, final String recordOperationProductModelName) {
        List<Entity> recordOperationProducts = Lists.newArrayList();

        String operationProductModel = null;
        String recordOperationProductsFieldName = null;

        if (MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT.equals(recordOperationProductModelName)) {
            operationProductModel = "operationProductInComponent";
            recordOperationProductsFieldName = RECORD_OPERATION_PRODUCT_IN_COMPONENTS;
        } else if (MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT.equals(recordOperationProductModelName)) {
            operationProductModel = "operationProductOutComponent";
            recordOperationProductsFieldName = RECORD_OPERATION_PRODUCT_OUT_COMPONENTS;
        }

        Map<Entity, BigDecimal> productComponentQuantities = productQuantitiesService
                .getProductComponentQuantities(asList(order));

        Set<Long> alreadyAddedProducts = Sets.newHashSet();

        for (Entry<Entity, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            Entity operationProductComponent = productComponentQuantity.getKey();

            if (technologyInstanceOperationComponent != null) {
                Entity operation = technologyInstanceOperationComponent.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT);

                Entity currentOperation = operationProductComponent.getBelongsToField(OPERATION_COMPONENT);

                if (!operation.getId().equals(currentOperation.getId())) {
                    continue;
                }
            }

            if (operationProductModel.equals(operationProductComponent.getDataDefinition().getName())) {
                Entity product = operationProductComponent.getBelongsToField(PRODUCT);

                if (!alreadyAddedProducts.contains(product.getId())) {
                    createRecordOperationProduct(recordOperationProducts, recordOperationProductModelName, product);

                    alreadyAddedProducts.add(product.getId());
                }
            }
        }

        productionRecord.setField(recordOperationProductsFieldName, recordOperationProducts);
    }

    private void createRecordOperationProduct(final List<Entity> recordOperationProducts,
            final String recordOperationProductModelName, final Entity product) {
        Entity recordOperationProduct = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                recordOperationProductModelName).create();

        recordOperationProduct.setField(PRODUCT, product);

        recordOperationProducts.add(recordOperationProduct);
    }

    public boolean checkIfOperationIsSet(final DataDefinition productionRecordDD, final Entity productionRecord) {
        String recordingMode = productionRecord.getBelongsToField(ORDER).getStringField(TYPE_OF_PRODUCTION_RECORDING);
        Object orderOperation = productionRecord.getField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        if (FOR_EACH.getStringValue().equals(recordingMode) && orderOperation == null) {
            productionRecord.addError(productionRecordDD.getField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT),
                    "productionCounting.productionRecord.messages.error.operationIsNotSet");
            return false;
        }
        return true;
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

    private boolean shouldCopy(final Entity productionRecord, final Entity order,
            final Entity technologyInstanceOperationComponent) {
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

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

}
