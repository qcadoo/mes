package com.qcadoo.mes.productionScheduling;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OrderRealizationTimeServiceImpl implements OrderRealizationTimeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private TranslationService translationService;

    @Override
    public void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateTo = (FieldComponent) state;
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("dateFrom");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        if (StringUtils.hasText((String) dateTo.getFieldValue()) && !StringUtils.hasText((String) dateFrom.getFieldValue())) {
            Date date = shiftsService.findDateFromForOrder(getDateFromField(dateTo.getFieldValue()),
                    Integer.valueOf((String) realizationTime.getFieldValue()));
            if (date != null) {
                dateFrom.setFieldValue(setDateToField(date));
            }
        }
    }

    @Override
    public void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateFrom = (FieldComponent) state;
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        if (!StringUtils.hasText((String) dateTo.getFieldValue()) && StringUtils.hasText((String) dateFrom.getFieldValue())) {
            Date date = shiftsService.findDateToForOrder(getDateFromField(dateFrom.getFieldValue()),
                    Integer.valueOf((String) realizationTime.getFieldValue()));
            if (date != null) {
                dateTo.setFieldValue(setDateToField(date));
            }
        }
    }

    @Override
    public Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(date);
    }

    @Override
    public Date getDateFromField(final Object value) {
        try {
            return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).parse((String) value);
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void countTimeOfTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference("quantity");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");

        int maxPathTime = 0;
        if (StringUtils.hasText((String) quantity.getFieldValue())) {
            Long id = (Long) state.getFieldValue();
            if (id != null) {
                Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY).get(id);
                maxPathTime = estimateRealizationTimeForTechnologyOperation(technology.getTreeField("operationComponents")
                        .getRoot(), getBigDecimalFromField(quantity.getFieldValue(), viewDefinitionState.getLocale()), 0, null);
            }
            if (maxPathTime > MAX_REALIZATION_TIME) {
                state.addMessage(
                        translationService.translate("orders.validate.global.error.RealizationTimeIsToLong",
                                viewDefinitionState.getLocale()), MessageType.FAILURE);
            }
        }
        realizationTime.setFieldValue(maxPathTime);
    }

    @Override
    @Transactional
    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("plannedQuantity");
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");

        int maxPathTime = 0;
        if (technology.getFieldValue() != null && StringUtils.hasText((String) plannedQuantity.getFieldValue())) {
            Long orderId = (Long) state.getFieldValue();
            if (orderId != null) {
                Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                        orderId);
                maxPathTime = estimateRealizationTimeForOperation(order.getTreeField("orderOperationComponents").getRoot(),
                        getBigDecimalFromField(plannedQuantity.getFieldValue(), viewDefinitionState.getLocale()));
            }
            if (maxPathTime > MAX_REALIZATION_TIME) {
                state.addMessage(
                        translationService.translate("orders.validate.global.error.RealizationTimeIsToLong",
                                viewDefinitionState.getLocale()), MessageType.FAILURE);
            }
        }
        realizationTime.setFieldValue(maxPathTime);
    }

    @Override
    public int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity) {
        int operationTime = 0;
        int pathTime = 0;

        for (EntityTreeNode child : operationComponent.getChildren()) {
            int tmpPathTime = estimateRealizationTimeForOperation(child, plannedQuantity);
            if (tmpPathTime > pathTime) {
                pathTime = tmpPathTime;
            }
        }

        if (operationComponent.getField("useMachineNorm") != null && (Boolean) operationComponent.getField("useMachineNorm")) {
            DataDefinition machineInOrderOperationComponentDD = dataDefinitionService.get("productionScheduling",
                    "machineInOrderOperationComponent");
            List<Entity> machineComponents = machineInOrderOperationComponentDD.find()
                    .add(SearchRestrictions.belongsTo("orderOperationComponent", operationComponent))
                    .addOrder(SearchOrders.asc("priority")).list().getEntities();
            for (Entity machineComponent : machineComponents) {
                if ((Boolean) machineComponent.getField("isActive")) {
                    operationComponent.setField("tj",
                            (machineComponent.getField("parallel") != null ? (BigDecimal) machineComponent.getField("parallel")
                                    : BigDecimal.ONE).multiply(BigDecimal.valueOf((Integer) machineComponent.getField("tj")))
                                    .intValue());
                    operationComponent.setField("tpz", machineComponent.getField("tpz"));
                    break;
                }
            }
        }

        if ("01all".equals(operationComponent.getField("countRealized"))) {
            operationTime = (plannedQuantity.multiply(BigDecimal.valueOf(getIntegerValue(operationComponent.getField("tj")))))
                    .intValue();
        } else {
            operationTime = ((operationComponent.getField("countMachine") != null ? (BigDecimal) operationComponent
                    .getField("countMachine") : BigDecimal.ZERO).multiply(BigDecimal.valueOf(getIntegerValue(operationComponent
                    .getField("tj"))))).intValue();
        }
        operationTime += getIntegerValue(operationComponent.getField("tpz"))
                + getIntegerValue(operationComponent.getField("timeNextOperation"));

        operationComponent.setField("effectiveOperationRealizationTime", operationTime);
        operationComponent.setField("operationOffSet", pathTime);
        DataDefinition orderOperationComponentDD = dataDefinitionService.get("productionScheduling", "orderOperationComponent");
        orderOperationComponentDD.save(operationComponent);

        pathTime += operationTime;
        return pathTime;
    }

    private int estimateRealizationTimeForTechnologyOperation(final EntityTreeNode operationComponent,
            final BigDecimal plannedQuantity, final int pathTime, final EntityTreeNode parent) {
        int operationTime = 0;
        // TODO KRNA maszyny

        // TODO KRNA podtechnologie
        if (operationComponent.getField("useDefaultValue") != null && (Boolean) operationComponent.getField("useDefaultValue")) {
            Entity operation = operationComponent.getBelongsToField("operation");
            if ("01all".equals(operation.getField("countRealizedOperation"))) {
                operationTime = (plannedQuantity.multiply(BigDecimal.valueOf(getIntegerValue(operationComponent.getField("tj")))))
                        .intValue()
                        + getIntegerValue(operationComponent.getField("tpz"))
                        + getIntegerValue(operationComponent.getField("timeNextOperation"));
            } else {
                operationTime = ((operationComponent.getField("countMachineOperation") != null ? (BigDecimal) operationComponent
                        .getField("countMachineOperation") : BigDecimal.ZERO).multiply(BigDecimal
                        .valueOf(getIntegerValue(operationComponent.getField("tj"))))).intValue()
                        + getIntegerValue(operationComponent.getField("tpz"))
                        + getIntegerValue(operationComponent.getField("timeNextOperation"));
            }
        } else {
            if ("01all".equals(operationComponent.getField("countRealizedNorm"))) {
                operationTime = (plannedQuantity.multiply(BigDecimal.valueOf(getIntegerValue(operationComponent.getField("tj")))))
                        .intValue()
                        + getIntegerValue(operationComponent.getField("tpz"))
                        + getIntegerValue(operationComponent.getField("timeNextOperationNorm"));
            } else {
                operationTime = ((operationComponent.getField("countMachineNorm") != null ? (BigDecimal) operationComponent
                        .getField("countMachineNorm") : BigDecimal.ZERO).multiply(BigDecimal
                        .valueOf(getIntegerValue(operationComponent.getField("tj"))))).intValue()
                        + getIntegerValue(operationComponent.getField("tpz"))
                        + getIntegerValue(operationComponent.getField("timeNextOperationNorm"));
            }
        }

        operationTime += pathTime;

        for (EntityTreeNode child : operationComponent.getChildren()) {
            int tmpPathTime = estimateRealizationTimeForTechnologyOperation(child, plannedQuantity, operationTime,
                    operationComponent);
            if (tmpPathTime > operationTime) {
                operationTime = tmpPathTime;
            }
        }

        return operationTime;
    }

    private Integer getIntegerValue(final Object value) {
        return value != null ? (Integer) value : Integer.valueOf(0);
    }

    @Override
    public BigDecimal getBigDecimalFromField(final Object value, final Locale locale) {
        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);
            return new BigDecimal(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
