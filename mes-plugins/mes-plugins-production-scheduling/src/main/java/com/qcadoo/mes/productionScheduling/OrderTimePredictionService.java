/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.productionScheduling;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class OrderTimePredictionService {

    private static final String TECHNOLOGY_COMPONENT = "technology";

    private static final String QUANTITY_COMPONENT = "quantity";

    private static final String DATE_FROM_COMPONENT = "dateFrom";

    private static final String DATE_TO_COMPONENT = "dateTo";

    private static final String PLANNED_QUANTITY_COMPONENT = "plannedQuantity";

    private static final String REALIZATION_TIME_COMPONENT = "realizationTime";

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Autowired
    private TimeConverterService timeConverterService;

    public void setFieldDisable(final ViewDefinitionState viewDefinitionState) {
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY_COMPONENT);

        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference(PLANNED_QUANTITY_COMPONENT);
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_FROM_COMPONENT);
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_TO_COMPONENT);
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT);

        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonActionItem countTimeOfTechnology = window.getRibbon().getGroupByName("timeOfTechnology")
                .getItemByName("countTimeOfTechnology");

        quantity.setRequired(true);
        dateFrom.setRequired(true);
        technology.setRequired(true);

        quantity.setEnabled(true);
        dateFrom.setEnabled(true);
        dateTo.setEnabled(false);
        realizationTime.setEnabled(false);
        countTimeOfTechnology.setEnabled(false);

        quantity.requestComponentUpdateState();
        dateFrom.requestComponentUpdateState();
        dateTo.requestComponentUpdateState();
        technology.requestComponentUpdateState();
        realizationTime.requestComponentUpdateState();
    }

    public void clearAllField(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY_COMPONENT);

        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference(QUANTITY_COMPONENT);
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_FROM_COMPONENT);
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_TO_COMPONENT);
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT);

        quantity.setFieldValue("");
        dateFrom.setFieldValue("");
        dateTo.setFieldValue("");
        realizationTime.setFieldValue("");
        technology.setFieldValue("");
    }

    private void scheduleOrder(final Long orderId) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

        if (order == null) {
            return;
        }

        DataDefinition dataDefinition = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        List<Entity> operations = dataDefinition.find().add(SearchRestrictions.belongsTo(OrdersConstants.MODEL_ORDER, order))
                .list().getEntities();

        Date orderStartDate = null;

        if (order.getField("effectiveDateFrom") == null) {
            if (order.getField("dateFrom") == null) {
                return;
            } else {
                orderStartDate = (Date) order.getField("dateFrom");
            }
        } else {
            orderStartDate = (Date) order.getField("effectiveDateFrom");
        }

        for (Entity operation : operations) {
            Integer offset = (Integer) operation.getField("operationOffSet");
            Integer duration = (Integer) operation.getField("effectiveOperationRealizationTime");

            operation.setField("effectiveDateFrom", null);
            operation.setField("effectiveDateTo", null);

            if (offset == null || duration == null || duration.equals(0)) {
                continue;
            }

            if (offset == 0) {
                offset = 1;
            }

            Date dateFrom = shiftsService.findDateToForOrder(orderStartDate, offset);

            if (dateFrom == null) {
                continue;
            }

            Date dateTo = shiftsService.findDateToForOrder(orderStartDate, offset + duration);

            if (dateTo == null) {
                continue;
            }

            operation.setField("effectiveDateFrom", dateFrom);
            operation.setField("effectiveDateTo", dateTo);
        }

        for (Entity operation : operations) {
            dataDefinition.save(operation);
        }
    }

    public void copyRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent generatedEndDate = (FieldComponent) viewDefinitionState.getComponentByReference("generatedEndDate");
        FieldComponent endDate = (FieldComponent) viewDefinitionState.getComponentByReference("stopTime");
        endDate.setFieldValue(generatedEndDate.getFieldValue());

        state.performEvent(viewDefinitionState, "save", new String[0]);
    }

    @Transactional
    public void generateRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("plannedQuantity");
        FieldComponent productionLineLookup = (FieldComponent) viewDefinitionState.getComponentByReference("productionLine");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        FieldComponent generatedEndDate = (FieldComponent) viewDefinitionState.getComponentByReference("generatedEndDate");
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("startTime");
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("stopTime");

        Entity productionLine = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get((Long) productionLineLookup.getFieldValue());

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntity().getId());

        BigDecimal quantity = orderRealizationTimeService.getBigDecimalFromField(plannedQuantity.getFieldValue(),
                viewDefinitionState.getLocale());

        int maxPathTime = orderRealizationTimeService.estimateMaxOperationTimeConsumptionForWorkstation(
                order.getTreeField("technologyInstanceOperationComponents").getRoot(), quantity, true, true, productionLine);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);
            realizationTime.setFieldValue(null);
            generatedEndDate.setFieldValue(null);
            realizationTime.requestComponentUpdateState();
            generatedEndDate.requestComponentUpdateState();
        } else {
            order.setField("realizationTime", maxPathTime);
            Date startTime = (Date) order.getField("dateFrom");
            Date stopTime = (Date) order.getField("dateTo");
            if (startTime == null) {
                dateFrom.addMessage("orders.validate.global.error.dateFromIsNull", MessageType.FAILURE);
            } else {
                Date generatedStopTime = shiftsService.findDateToForOrder(startTime, maxPathTime);

                if (generatedStopTime != null) {
                    if (stopTime == null) {

                        order.setField("dateTo", orderRealizationTimeService.setDateToField(generatedStopTime));
                        realizationTime.setFieldValue(maxPathTime);
                        dateTo.setFieldValue(orderRealizationTimeService.setDateToField(generatedStopTime));
                        generatedEndDate.setFieldValue(orderRealizationTimeService.setDateToField(generatedStopTime));

                    }
                    order.setField("generatedEndDate", orderRealizationTimeService.setDateToField(generatedStopTime));

                    scheduleOrder(order.getId());
                }
                generatedEndDate.setFieldValue(orderRealizationTimeService.setDateToField(generatedStopTime));
                realizationTime.setFieldValue(maxPathTime);
                realizationTime.requestComponentUpdateState();
                dateTo.requestComponentUpdateState();
                generatedEndDate.requestComponentUpdateState();

            }
            order.getDataDefinition().save(order);

        }

    }

    @Transactional
    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY_COMPONENT);
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference(QUANTITY_COMPONENT);
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_FROM_COMPONENT);
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_TO_COMPONENT);
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT);
        FieldComponent productionLineLookup = (FieldComponent) viewDefinitionState.getComponentByReference("productionLine");

        if (technologyLookup.getFieldValue() == null) {
            technologyLookup.addMessage("productionScheduling.error.fieldRequired", MessageType.FAILURE);
            return;
        }

        if (!StringUtils.hasText((String) dateFrom.getFieldValue())) {
            dateFrom.addMessage("productionScheduling.error.fieldRequired", MessageType.FAILURE);
            return;
        }

        if (!StringUtils.hasText((String) plannedQuantity.getFieldValue())) {
            plannedQuantity.addMessage("productionScheduling.error.fieldRequired", MessageType.FAILURE);
            return;
        }

        if (productionLineLookup.getFieldValue() == null) {
            productionLineLookup.addMessage("productionScheduling.error.fieldRequired", MessageType.FAILURE);
            return;
        }

        BigDecimal quantity = orderRealizationTimeService.getBigDecimalFromField(plannedQuantity.getFieldValue(),
                viewDefinitionState.getLocale());

        if (quantity.intValue() < 0) {
            plannedQuantity.addMessage("productionScheduling.error.fieldRequired", MessageType.FAILURE);
            return;
        }

        int maxPathTime = 0;

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyLookup.getFieldValue());

        if (technology.getStringField(TechnologyFields.STATE).equals(TechnologyState.DRAFT.getStringValue())
                || technology.getStringField(TechnologyFields.STATE).equals(TechnologyState.OUTDATED.getStringValue())) {
            technologyLookup.addMessage("productionScheduling.technology.incorrectState", MessageType.FAILURE);
            return;
        }

        Entity productionLine = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get((Long) productionLineLookup.getFieldValue());

        maxPathTime = orderRealizationTimeService.estimateOperationTimeConsumption(technology.getTreeField("operationComponents")
                .getRoot(), quantity, productionLine);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);
            realizationTime.setFieldValue(null);
            dateTo.setFieldValue(null);
        } else {
            realizationTime.setFieldValue(maxPathTime);
            Date startTime = timeConverterService.getDateFromField(dateFrom.getFieldValue());
            Date stopTime = shiftsService.findDateToForOrder(startTime, maxPathTime);

            if (stopTime != null) {
                startTime = shiftsService.findDateFromForOrder(stopTime, maxPathTime);
            }

            if (startTime == null) {
                dateFrom.setFieldValue(null);
            } else {
                dateFrom.setFieldValue(orderRealizationTimeService.setDateToField(startTime));
            }

            if (stopTime == null) {
                dateTo.setFieldValue(null);
            } else {
                dateTo.setFieldValue(orderRealizationTimeService.setDateToField(stopTime));
            }
        }
    }

    public void fillUnitField(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent unitField = (FieldComponent) viewDefinitionState.getComponentByReference("operationDurationQuantityUNIT");

        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntity().getId());
        Entity product = order.getBelongsToField("product");

        unitField.setFieldValue(product.getField("unit"));
    }

    public void fillUnitField(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent unitField = (FieldComponent) viewDefinitionState.getComponentByReference("operationDurationQuantityUNIT");

        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyLookup.getFieldValue());

        Entity product = technology.getBelongsToField("product");

        unitField.setFieldValue(product.getField("unit"));
    }

    public void disableRealizationTime(final ViewDefinitionState viewDefinitionState) {
        viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT).setEnabled(false);
    }

    public void clearFieldValue(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        state.setFieldValue(null);
    }
}
