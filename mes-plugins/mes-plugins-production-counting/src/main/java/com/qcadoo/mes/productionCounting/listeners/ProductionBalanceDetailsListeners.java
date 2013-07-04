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
package com.qcadoo.mes.productionCounting.listeners;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.ProductionCountingGenerateProductionBalance;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OperationPieceworkComponentFields;
import com.qcadoo.mes.productionCounting.constants.OperationTimeComponentFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.print.ProductionBalancePdfService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionBalanceDetailsListeners {

    private static final String L_PRODUCT = "product";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_BALANCE = "balance";

    private static final String L_PLANNED_MACHINE_TIME = "plannedMachineTime";

    private static final String L_PLANNED_LABOR_TIME = "plannedLaborTime";

    private static final String L_EMPTY_NUMBER = "";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private ProductionCountingGenerateProductionBalance generateProductionBalance;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionBalanceService productionBalanceService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Transactional
    public void generateProductionBalance(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save", new String[0]);

        if (!state.isHasError()) {
            Long productionBalanceId = (Long) state.getFieldValue();

            Entity productionBalance = productionCountingService.getProductionBalance(productionBalanceId);

            if (productionBalance == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(productionBalance.getStringField(ProductionBalanceFields.FILE_NAME))) {
                state.addMessage("productionCounting.productionBalance.report.error.documentsWasGenerated", MessageType.FAILURE);
                return;
            }

            if (!productionBalance.getBooleanField(ProductionBalanceFields.GENERATED)) {
                fillReportValues(productionBalance);

                fillFieldsAndGrids(productionBalance);

                productionBalance.getDataDefinition().save(productionBalance);
            }

            productionBalance = productionCountingService.getProductionBalance(productionBalanceId);

            checkOrderDoneQuantity(state, productionBalance);

            try {
                generateProductionBalanceDocuments(productionBalance, state.getLocale());

                state.performEvent(view, "reset", new String[0]);

                state.addMessage(
                        "productionCounting.productionBalanceDetails.window.mainTab.productionBalanceDetails.generatedMessage",
                        MessageType.SUCCESS);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void fillReportValues(final Entity productionBalance) {
        productionBalance.setField(ProductionBalanceFields.GENERATED, true);
        productionBalance.setField(ProductionBalanceFields.DATE, new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
                LocaleContextHolder.getLocale()).format(new Date()));
        productionBalance.setField(ProductionBalanceFields.WORKER, securityService.getCurrentUserName());
    }

    private void fillFieldsAndGrids(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        if ((order == null)
                || productionCountingService.isTypeOfProductionRecordingBasic(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            return;
        }

        List<Entity> productionTrackings = productionCountingService.getProductionTrackingsForOrder(order);

        Map<Long, Entity> productionTrackingsWithRegisteredTimes = productionBalanceService
                .groupProductionTrackingsRegisteredTimes(productionBalance, productionTrackings);

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)) {
            fillBalanceOperationProductComponents(productionBalance, productionTrackings,
                    ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS,
                    ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_IN_COMPONENTS,
                    ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT);
        }

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)) {
            fillBalanceOperationProductComponents(productionBalance, productionTrackings,
                    ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS,
                    ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS,
                    ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_OUT_COMPONENT);
        }

        if (productionCountingService.isCalculateOperationCostModeHourly(productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
            Map<Long, Map<String, Integer>> productionTrackingsWithPlannedTimes = productionBalanceService
                    .fillProductionTrackingsWithPlannedTimes(productionBalance, productionTrackings);

            calculatePlannedTimeValues(productionBalance);
            fillTimeValues(productionBalance, productionTrackingsWithRegisteredTimes, productionTrackingsWithPlannedTimes);

            if (productionCountingService.isTypeOfProductionRecordingForEach(order
                    .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                fillOperationTimeComponents(productionBalance, productionTrackingsWithRegisteredTimes,
                        productionTrackingsWithPlannedTimes);
            }
        } else if (productionCountingService.isCalculateOperationCostModePiecework(productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)) {
            fillOperationPieceworkComponents(productionBalance, productionTrackingsWithRegisteredTimes);
        }
    }

    private void fillBalanceOperationProductComponents(final Entity productionBalance, final List<Entity> productionTrackings,
            final String trackingOperationProductComponentsModel, final String balanceOperationProductComponentsModel,
            final String balanceOperationProductComponentModel) {
        if (productionBalance == null) {
            return;
        }

        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        Map<Long, Entity> balanceOperationProductComponents = Maps.newHashMap();
        Set<Long> addedTechnologyOperationComponents = Sets.newHashSet();

        boolean shouldAddPlannedQuantity = true;

        for (Entity productionTracking : productionTrackings) {
            List<Entity> trackingOperationProductComponents = productionTracking
                    .getHasManyField(trackingOperationProductComponentsModel);

            Entity technologyOperationComponent = productionTracking
                    .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

            if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
                if (addedTechnologyOperationComponents.contains(technologyOperationComponent.getId())) {
                    shouldAddPlannedQuantity = false;
                } else {
                    shouldAddPlannedQuantity = true;
                }
            }

            if (trackingOperationProductComponents != null) {
                for (Entity trackingOperationProductComponent : trackingOperationProductComponents) {
                    Entity product = trackingOperationProductComponent.getBelongsToField(L_PRODUCT);

                    if (product != null) {
                        Long productId = product.getId();

                        if (balanceOperationProductComponents.containsKey(productId)) {
                            updateBalanceOperationComponent(balanceOperationProductComponents, trackingOperationProductComponent,
                                    productId, shouldAddPlannedQuantity);
                        } else {
                            addBalanceOperationComponent(balanceOperationProductComponents,
                                    balanceOperationProductComponentModel, trackingOperationProductComponent, productId);
                        }
                    }
                }
            }

            if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
                shouldAddPlannedQuantity = false;
            } else {
                addedTechnologyOperationComponents.add(technologyOperationComponent.getId());
            }
        }

        productionBalance.setField(balanceOperationProductComponentsModel,
                Lists.newArrayList(balanceOperationProductComponents.values()));
    }

    private void addBalanceOperationComponent(final Map<Long, Entity> balanceOperationProductComponents,
            final String balanceOperationProductComponentModel, final Entity trackingOperationProductComponent,
            final Long productId) {
        BigDecimal plannedQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductComponent
                .getDecimalField(L_PLANNED_QUANTITY));
        BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductComponent
                .getDecimalField(L_USED_QUANTITY));

        BigDecimal balance = usedQuantity.subtract(plannedQuantity, numberService.getMathContext());

        Entity balanceOperationProductComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                balanceOperationProductComponentModel).create();

        balanceOperationProductComponent.setField(L_PRODUCT, trackingOperationProductComponent.getField(L_PRODUCT));

        balanceOperationProductComponent.setField(L_PLANNED_QUANTITY, numberService.setScale(plannedQuantity));
        balanceOperationProductComponent.setField(L_USED_QUANTITY, numberService.setScale(usedQuantity));
        balanceOperationProductComponent.setField(L_BALANCE, numberService.setScale(balance));

        balanceOperationProductComponents.put(productId, balanceOperationProductComponent);
    }

    private void updateBalanceOperationComponent(final Map<Long, Entity> balanceOperationProductComponents,
            final Entity trackingOperationProductComponent, final Long productId, final boolean shouldAddPlannedQuantity) {
        Entity addedBalanceOperationProductInComponent = balanceOperationProductComponents.get(productId);

        BigDecimal plannedQuantity = addedBalanceOperationProductInComponent.getDecimalField(L_PLANNED_QUANTITY);
        BigDecimal usedQuantity = addedBalanceOperationProductInComponent.getDecimalField(L_USED_QUANTITY);

        if (shouldAddPlannedQuantity) {
            plannedQuantity = plannedQuantity.add(
                    BigDecimalUtils.convertNullToZero(trackingOperationProductComponent.getDecimalField(L_PLANNED_QUANTITY)),
                    numberService.getMathContext());
        }

        usedQuantity = usedQuantity.add(
                BigDecimalUtils.convertNullToZero(trackingOperationProductComponent.getDecimalField(L_USED_QUANTITY)),
                numberService.getMathContext());

        BigDecimal balance = usedQuantity.subtract(plannedQuantity, numberService.getMathContext());

        addedBalanceOperationProductInComponent.setField(L_PLANNED_QUANTITY, numberService.setScale(plannedQuantity));
        addedBalanceOperationProductInComponent.setField(L_USED_QUANTITY, numberService.setScale(usedQuantity));
        addedBalanceOperationProductInComponent.setField(L_BALANCE, numberService.setScale(balance));

        balanceOperationProductComponents.put(productId, addedBalanceOperationProductInComponent);
    }

    private void calculatePlannedTimeValues(final Entity productionBalance) {
        final Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        final boolean includeTpz = productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ);
        final boolean includeAdditionalTime = productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);
        final Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        // TODO LUPO fix problem with operationRuns
        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getNeededProductQuantities(Lists.newArrayList(order), MrpAlgorithm.ONLY_COMPONENTS,
                operationRuns);

        final OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTotalWorkTimeForOrder(order, operationRuns,
                includeTpz, includeAdditionalTime, productionLine, false);

        final int plannedMachineTime = operationWorkTime.getMachineWorkTime();
        final int plannedLaborTime = operationWorkTime.getLaborWorkTime();

        productionBalance.setField(ProductionBalanceFields.PLANNED_LABOR_TIME, plannedLaborTime);
        productionBalance.setField(ProductionBalanceFields.PLANNED_MACHINE_TIME, plannedMachineTime);
    }

    private void fillTimeValues(final Entity productionBalance, final Map<Long, Entity> productionTrackingsWithRegisteredTimes,
            final Map<Long, Map<String, Integer>> productionTrackingsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }
        Integer machineTime = 0;
        Integer laborTime = 0;

        if (!productionTrackingsWithPlannedTimes.isEmpty()) {
            for (Entry<Long, Entity> productionTrackingWithRegisteredTimes : productionTrackingsWithRegisteredTimes.entrySet()) {
                Entity productionTracking = productionTrackingWithRegisteredTimes.getValue();
                machineTime += productionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME);
                laborTime += productionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME);
            }
        }

        final int machineTimeBalance = machineTime
                - productionBalance.getIntegerField(ProductionBalanceFields.PLANNED_MACHINE_TIME);
        final int laborTimeBalance = laborTime - productionBalance.getIntegerField(ProductionBalanceFields.PLANNED_LABOR_TIME);

        productionBalance.setField(ProductionBalanceFields.MACHINE_TIME, machineTime);
        productionBalance.setField(ProductionBalanceFields.MACHINE_TIME_BALANCE, machineTimeBalance);

        productionBalance.setField(ProductionBalanceFields.LABOR_TIME, laborTime);
        productionBalance.setField(ProductionBalanceFields.LABOR_TIME_BALANCE, laborTimeBalance);
    }

    private void fillOperationTimeComponents(final Entity productionBalance,
            final Map<Long, Entity> productionTrackingsWithRegisteredTimes,
            final Map<Long, Map<String, Integer>> productionTrackingsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationTimeComponents = Lists.newArrayList();

        if (!productionTrackingsWithPlannedTimes.isEmpty()) {
            for (Entry<Long, Entity> productionTrackingWithRegisteredTimes : productionTrackingsWithRegisteredTimes.entrySet()) {
                Entity productionTracking = productionTrackingWithRegisteredTimes.getValue();

                Entity technologyOperationComponent = productionTracking
                        .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (technologyOperationComponent != null) {
                    Long technologyOperationComponentId = technologyOperationComponent.getId();

                    if (productionTrackingsWithPlannedTimes.containsKey(technologyOperationComponentId)) {
                        Integer plannedMachineTime = productionTrackingsWithPlannedTimes.get(technologyOperationComponentId).get(
                                L_PLANNED_MACHINE_TIME);
                        Integer machineTime = productionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME);

                        Integer machineTimeBalance = machineTime - plannedMachineTime;

                        Integer plannedLaborTime = productionTrackingsWithPlannedTimes.get(technologyOperationComponentId).get(
                                L_PLANNED_LABOR_TIME);
                        Integer laborTime = productionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME);

                        Integer laborTimeBalance = laborTime - plannedLaborTime;

                        Entity operationTimeComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_OPERATION_TIME_COMPONENT).create();

                        operationTimeComponent.setField(OperationPieceworkComponentFields.TECHNOLOGY_OPERATION_COMPONENT,
                                technologyOperationComponent);

                        operationTimeComponent.setField(OperationTimeComponentFields.PLANNED_MACHINE_TIME, plannedMachineTime);
                        operationTimeComponent.setField(OperationTimeComponentFields.MACHINE_TIME, machineTime);
                        operationTimeComponent.setField(OperationTimeComponentFields.MACHINE_TIME_BALANCE, machineTimeBalance);

                        operationTimeComponent.setField(OperationTimeComponentFields.PLANNED_LABOR_TIME, plannedLaborTime);
                        operationTimeComponent.setField(OperationTimeComponentFields.LABOR_TIME, laborTime);
                        operationTimeComponent.setField(OperationTimeComponentFields.LABOR_TIME_BALANCE, laborTimeBalance);

                        operationTimeComponents.add(operationTimeComponent);
                    }
                }
            }
        }

        productionBalance.setField(ProductionBalanceFields.OPERATION_TIME_COMPONENTS, operationTimeComponents);
    }

    private void fillOperationPieceworkComponents(final Entity productionBalance,
            final Map<Long, Entity> productionTrackingsWithRegisteredTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationPieceworkComponents = Lists.newArrayList();

        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        Map<Long, BigDecimal> productComponents = productQuantitiesService.getProductComponentQuantities(
                asList(productionBalance.getBelongsToField(ProductionBalanceFields.ORDER)), operationRuns);

        if (!productComponents.isEmpty()) {
            for (Entry<Long, Entity> productionTrackingWithRegisteredTimes : productionTrackingsWithRegisteredTimes.entrySet()) {
                Entity productionTracking = productionTrackingWithRegisteredTimes.getValue();

                Entity technologyOperationComponent = productionTracking
                        .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (technologyOperationComponent != null) {
                    Long technologyOperationComponentId = technologyOperationComponent.getId();

                    if (operationRuns.containsKey(technologyOperationComponentId)) {
                        BigDecimal plannedCycles = operationRuns.get(technologyOperationComponentId);

                        BigDecimal cycles = productionTracking
                                .getDecimalField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES);

                        BigDecimal cyclesBalance = cycles.subtract(plannedCycles, numberService.getMathContext());

                        Entity operationPieceworkComponent = dataDefinitionService.get(
                                ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_OPERATION_PIECEWORK_COMPONENT).create();

                        operationPieceworkComponent.setField(OperationPieceworkComponentFields.TECHNOLOGY_OPERATION_COMPONENT,
                                technologyOperationComponent);

                        operationPieceworkComponent.setField(OperationPieceworkComponentFields.PLANNED_CYCLES,
                                numberService.setScale(plannedCycles));
                        operationPieceworkComponent.setField(OperationPieceworkComponentFields.CYCLES,
                                numberService.setScale(cycles));
                        operationPieceworkComponent.setField(OperationPieceworkComponentFields.CYCLES_BALANCE,
                                numberService.setScale(cyclesBalance));

                        operationPieceworkComponents.add(operationPieceworkComponent);
                    }
                }
            }
        }

        productionBalance.setField(ProductionBalanceFields.OPERATION_PIECEWORK_COMPONENTS, operationPieceworkComponents);
    }

    private void checkOrderDoneQuantity(final ComponentState componentState, final Entity productionBalance) {
        final Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        final BigDecimal doneQuantityFromOrder = order.getDecimalField(OrderFields.DONE_QUANTITY);
        if (doneQuantityFromOrder == null || BigDecimal.ZERO.compareTo(doneQuantityFromOrder) == 0) {
            componentState.addMessage("productionCounting.productionBalance.report.info.orderWithoutDoneQuantity",
                    MessageType.INFO);
        }
    }

    private void generateProductionBalanceDocuments(final Entity productionBalance, final Locale locale) throws IOException,
            DocumentException {
        String localePrefix = "productionCounting.productionBalance.report.fileName";

        Entity productionBalanceWithFileName = fileService.updateReportFileName(productionBalance, ProductionBalanceFields.DATE,
                localePrefix);

        try {
            productionBalancePdfService.generateDocument(productionBalanceWithFileName, locale);

            generateProductionBalance.notifyObserversThatTheBalanceIsBeingGenerated(productionBalance);
        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving productionBalance report");
        } catch (DocumentException e) {
            throw new IllegalStateException("Problem with generating productionBalance report");
        }
    }

    public void printProductionBalance(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, args[1] });
    }

    public void fillProductAndTrackingsNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent orderLookup = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.ORDER);

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            clearProductAndTrackingsNumber(view);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if (order == null) {
            clearProductAndTrackingsNumber(view);
            return;
        }

        if (productionCountingService.isTypeOfProductionRecordingBasic(order
                .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            clearProductAndTrackingsNumber(view);

            orderLookup.addMessage("productionCounting.productionBalance.report.error.orderWithoutRecordingType",
                    ComponentState.MessageType.FAILURE);

            return;
        }

        fillProductAndTrackingsNumber(view, order);
    }

    private void fillProductAndTrackingsNumber(final ViewDefinitionState view, final Entity order) {
        FieldComponent productField = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.PRODUCT);
        FieldComponent trackingsNumberField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.TRACKINGS_NUMBER);

        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        Integer trackingsNumber = productionCountingService.getProductionTrackingsForOrder(order).size();

        productField.setFieldValue(product.getId());
        trackingsNumberField.setFieldValue(trackingsNumber);
    }

    private void clearProductAndTrackingsNumber(final ViewDefinitionState view) {
        FieldComponent productField = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.PRODUCT);
        FieldComponent trackingsNumberField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.TRACKINGS_NUMBER);

        productField.setFieldValue(null);
        trackingsNumberField.setFieldValue(null);
    }

    public void setDefaultNameUsingOrder(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        if (!(component instanceof FieldComponent)) {
            return;
        }

        FieldComponent orderField = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.ORDER);
        FieldComponent name = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.NAME);

        if (orderField.getFieldValue() == null || StringUtils.hasText((String) name.getFieldValue())) {
            return;
        }

        Entity orderEntity = orderService.getOrder((Long) orderField.getFieldValue());

        if (orderEntity == null) {
            return;
        }

        Locale locale = component.getLocale();
        name.setFieldValue(makeDefaultName(orderEntity, locale));
    }

    public String makeDefaultName(final Entity order, final Locale locale) {
        String orderNumber = L_EMPTY_NUMBER;

        if (order != null) {
            orderNumber = order.getStringField(OrderFields.NUMBER);
        }

        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(new Date());

        return translationService.translate("productionCounting.productionBalance.name.default", locale, orderNumber,
                cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH));
    }

    public void disableCheckboxes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productionBalanceService.disableCheckboxes(view);
    }

}
