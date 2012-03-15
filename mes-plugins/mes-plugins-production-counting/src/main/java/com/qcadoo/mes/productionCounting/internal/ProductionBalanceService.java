/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceService {

    private static final String L_STATE = "state";

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_BALANCE = "balance";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private ProductionBalanceReportDataService productionBalanceReportDataService;

    @Autowired
    private ProductionCountingGenerateProductionBalance generateProductionBalance;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private NumberService numberService;

    public void clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(ProductionBalanceFields.FILE_NAME, null);
        entity.setField(ProductionBalanceFields.GENERATED, false);
        entity.setField(ProductionBalanceFields.DATE, null);
        entity.setField(ProductionBalanceFields.WORKER, null);
    }

    public boolean validateOrder(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField(MODEL_ORDER);

        if ((order == null) || checkIfTypeOfProductionRecordingIsBasic(order)) {
            entity.addError(dataDefinition.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRecordingType");

            return false;
        }

        List<Entity> productionRecordList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(L_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(MODEL_ORDER, order)).list().getEntities();

        if (productionRecordList.isEmpty()) {
            entity.addError(dataDefinition.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutProductionRecords");

            return false;
        }

        return true;
    }

    private Entity getProductionBalance(final Long id) {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE).get(id);
    }

    @Transactional
    public void generateProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            Entity productionBalance = getProductionBalance((Long) state.getFieldValue());

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
            }

            productionBalance = getProductionBalance((Long) state.getFieldValue());

            try {
                generateProductionBalanceDocuments(productionBalance, state.getLocale());

                state.performEvent(viewDefinitionState, "reset", new String[0]);

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

    public void printProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, args[1] });
    }

    private void fillReportValues(final Entity productionBalance) {
        productionBalance.setField(ProductionBalanceFields.GENERATED, true);
        productionBalance.setField(ProductionBalanceFields.DATE, new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
                LocaleContextHolder.getLocale()).format(new Date()));
        productionBalance.setField(ProductionBalanceFields.WORKER, securityService.getCurrentUserName());
    }

    private void fillFieldsAndGrids(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(OrdersConstants.MODEL_ORDER);

        if ((order == null) || checkIfTypeOfProductionRecordingIsBasic(order)) {
            return;
        }

        if (order.getBooleanField(ProductionCountingConstants.PARAM_REGISTER_IN_PRODUCTS)) {
            fillBalanceOperationProductInComponents(productionBalance, order);
        }

        if (order.getBooleanField(ProductionCountingConstants.PARAM_REGISTER_OUT_PRODUCTS)) {
            fillBalanceOperationProductOutComponents(productionBalance, order);
        }

        if (order.getBooleanField(ProductionCountingConstants.PARAM_REGISTER_TIME)) {
            if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
                fillTimeValues(productionBalance, order);
                fillOperationTimeComponents(productionBalance, order);
            } else if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                    order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
                fillTimeValues(productionBalance, order);
            }
        }
    }

    private void fillBalanceOperationProductInComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> balanceOperationProductInComponents = Lists.newArrayList();

        List<Entity> productionRecords = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(L_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(OrdersConstants.MODEL_ORDER, order)).list().getEntities();

        for (Entity productionRecord : productionRecords) {
            List<Entity> recordOperationProductInComponents = productionRecord
                    .getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS);

            if (recordOperationProductInComponents != null) {
                for (Entity recordOperationProductInComponent : recordOperationProductInComponents) {
                    Entity balanceOperationProductInComponent = dataDefinitionService.get(
                            ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT).create();

                    balanceOperationProductInComponent.setField(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE,
                            productionBalance);
                    balanceOperationProductInComponent.setField(ProductionCountingConstants.MODEL_PRODUCTION_RECORD,
                            recordOperationProductInComponent.getField(ProductionCountingConstants.MODEL_PRODUCTION_RECORD));
                    balanceOperationProductInComponent.setField(BasicConstants.MODEL_PRODUCT,
                            recordOperationProductInComponent.getField(BasicConstants.MODEL_PRODUCT));
                    balanceOperationProductInComponent.setField(L_USED_QUANTITY,
                            recordOperationProductInComponent.getField(L_USED_QUANTITY));
                    balanceOperationProductInComponent.setField(L_PLANNED_QUANTITY,
                            recordOperationProductInComponent.getField(L_PLANNED_QUANTITY));
                    balanceOperationProductInComponent.setField(L_BALANCE, recordOperationProductInComponent.getField(L_BALANCE));

                    balanceOperationProductInComponents.add(balanceOperationProductInComponent);
                }
            }
        }

        if (!balanceOperationProductInComponents.isEmpty()) {
            Collections.sort(balanceOperationProductInComponents, new EntityProductInOutComparator());

            productionBalance.setField(ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_IN_COMPONENTS,
                    productionBalanceReportDataService.groupProductInOutComponentsByProduct(balanceOperationProductInComponents));

            productionBalance.getDataDefinition().save(productionBalance);
        }
    }

    private void fillBalanceOperationProductOutComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> balanceOperationProductOutComponents = Lists.newArrayList();

        List<Entity> productionRecordList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(L_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(OrdersConstants.MODEL_ORDER, order)).list().getEntities();

        for (Entity productionRecord : productionRecordList) {
            List<Entity> recordOperationProductOutComponents = productionRecord
                    .getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS);

            if (recordOperationProductOutComponents != null) {
                for (Entity recordOperationProductOutComponent : recordOperationProductOutComponents) {
                    Entity balanceOperationProductOutComponent = dataDefinitionService.get(
                            ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_OUT_COMPONENT).create();

                    balanceOperationProductOutComponent.setField(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE,
                            productionBalance);
                    balanceOperationProductOutComponent.setField(ProductionCountingConstants.MODEL_PRODUCTION_RECORD,
                            recordOperationProductOutComponent.getField(ProductionCountingConstants.MODEL_PRODUCTION_RECORD));
                    balanceOperationProductOutComponent.setField(BasicConstants.MODEL_PRODUCT,
                            recordOperationProductOutComponent.getField(BasicConstants.MODEL_PRODUCT));
                    balanceOperationProductOutComponent.setField(L_USED_QUANTITY,
                            recordOperationProductOutComponent.getField(L_USED_QUANTITY));
                    balanceOperationProductOutComponent.setField(L_PLANNED_QUANTITY,
                            recordOperationProductOutComponent.getField(L_PLANNED_QUANTITY));
                    balanceOperationProductOutComponent.setField(L_BALANCE,
                            recordOperationProductOutComponent.getField(L_BALANCE));

                    balanceOperationProductOutComponents.add(balanceOperationProductOutComponent);
                }
            }
        }

        if (!balanceOperationProductOutComponents.isEmpty()) {
            Collections.sort(balanceOperationProductOutComponents, new EntityProductInOutComparator());

            productionBalance
                    .setField(ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS,
                            productionBalanceReportDataService
                                    .groupProductInOutComponentsByProduct(balanceOperationProductOutComponents));

            productionBalance.getDataDefinition().save(productionBalance);
        }
    }

    private void fillOperationTimeComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(L_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(OrdersConstants.MODEL_ORDER, order)).list().getEntities();

        if (!productionRecordsList.isEmpty()) {
            Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());

            productionBalance.setField(ProductionBalanceFields.OPERATION_TIME_COMPONENTS,
                    productionBalanceReportDataService.groupProductionRecordsByOperation(productionRecordsList));
        }
    }

    private void fillTimeValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        BigDecimal plannedMachineTime = BigDecimal.ZERO;
        BigDecimal machineTime = BigDecimal.ZERO;

        BigDecimal laborTime = BigDecimal.ZERO;
        BigDecimal plannedLaborTime = BigDecimal.ZERO;

        List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(L_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(OrdersConstants.MODEL_ORDER, order)).list().getEntities();

        for (Entity productionRecord : productionRecordsList) {
            plannedMachineTime = plannedMachineTime.add(
                    new BigDecimal((Integer) productionRecord.getField("plannedMachineTime")), numberService.getMathContext());
            plannedLaborTime = plannedLaborTime.add(new BigDecimal((Integer) productionRecord.getField("plannedLaborTime")),
                    numberService.getMathContext());

            machineTime = machineTime.add(new BigDecimal((Integer) productionRecord.getField("machineTime")),
                    numberService.getMathContext());
            laborTime = laborTime.add(new BigDecimal((Integer) productionRecord.getField("laborTime")),
                    numberService.getMathContext());
        }

        BigDecimal machineTimeBalance = machineTime.subtract(plannedMachineTime, numberService.getMathContext());
        BigDecimal laborTimeBalance = laborTime.subtract(plannedLaborTime, numberService.getMathContext());

        productionBalance.setField("plannedMachineTime", plannedMachineTime);
        productionBalance.setField("machineTime", machineTime);
        productionBalance.setField("machineTimeBalance", machineTimeBalance);

        productionBalance.setField("plannedLaborTime", plannedLaborTime);
        productionBalance.setField("laborTime", laborTime);
        productionBalance.setField("laborTimeBalance", laborTimeBalance);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    public boolean checkIfTypeOfProductionRecordingIsBasic(final Entity order) {
        return TypeOfProductionRecording.BASIC.getStringValue().equals(order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING));
    }

    private void generateProductionBalanceDocuments(final Entity productionBalance, final Locale locale) throws IOException,
            DocumentException {
        Entity productionBalanceWithFileName = fileService.updateReportFileName(productionBalance, ProductionBalanceFields.DATE,
                "productionCounting.productionBalance.report.fileName");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        productionBalancePdfService.generateDocument(productionBalanceWithFileName, company, locale);

        generateProductionBalance.notifyObserversThatTheBalanceIsBeingGenerated(productionBalance);
    }
}
