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
public class OrderRealizationTimeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private TranslationService translationService;

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

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(date);
    }

    private Date getDateFromField(final Object value) {
        try {
            return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).parse((String) value);
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Transactional
    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }

        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("plannedQuantity");
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");

        int maxPathTime = 0;
        if (technology.getFieldValue() != null && StringUtils.hasText((String) plannedQuantity.getFieldValue())) {
            Long orderId = (Long) viewDefinitionState.getComponentByReference("form").getFieldValue();
            if (orderId != null) {
                Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                        orderId);
                maxPathTime = estimateRealizationTimeForOperation(order.getTreeField("orderOperationComponents").getRoot(),
                        getBigDecimalFromField(plannedQuantity.getFieldValue(), viewDefinitionState.getLocale()), 0, null);
            }
            if (maxPathTime > 99999 * 60 * 60) {
                viewDefinitionState.getComponentByReference("form").addMessage(
                        translationService.translate("orders.validate.global.error.RealizationTimeIsToLong",
                                viewDefinitionState.getLocale()), MessageType.FAILURE);
            }
        }
        realizationTime.setFieldValue(maxPathTime);
    }

    private int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final int pathTime, final EntityTreeNode parent) {
        int operationTime = 0;
        operationComponent.setField("effectiveMachine", null);
        if (operationComponent.getField("useMachineNorm") != null && (Boolean) operationComponent.getField("useMachineNorm")) {
            DataDefinition machineInOrderOperationComponentDD = dataDefinitionService.get("productionScheduling",
                    "machineInOrderOperationComponent");
            List<Entity> machineComponents = machineInOrderOperationComponentDD.find()
                    .add(SearchRestrictions.belongsTo("orderOperationComponent", operationComponent))
                    .addOrder(SearchOrders.asc("priority")).list().getEntities();
            for (Entity machineComponent : machineComponents) {
                if ((Boolean) machineComponent.getField("isActive")) {
                    operationComponent.setField("tj", machineComponent.getField("tj"));
                    operationComponent.setField("tpz", machineComponent.getField("tpz"));
                    operationComponent.setField("effectiveMachine", machineComponent.getBelongsToField("machine"));
                    break;
                }
            }
        }

        if ("01all".equals(operationComponent.getField("countRealized"))) {
            operationTime = (plannedQuantity.multiply(BigDecimal.valueOf(getIntegerValue(operationComponent.getField("tj")))))
                    .intValue()
                    + getIntegerValue(operationComponent.getField("tpz"))
                    + getIntegerValue(operationComponent.getField("timeNextOperation"));
        } else {
            operationTime = ((operationComponent.getField("countMachine") != null ? (BigDecimal) operationComponent
                    .getField("countMachine") : BigDecimal.ZERO).multiply(BigDecimal.valueOf(getIntegerValue(operationComponent
                    .getField("tj"))))).intValue()
                    + getIntegerValue(operationComponent.getField("tpz"))
                    + getIntegerValue(operationComponent.getField("timeNextOperation"));
        }

        operationComponent.setField("effectiveOperationRealizationTime", operationTime);
        operationComponent.setField("operationOffSet", 0);
        DataDefinition orderOperationComponentDD = dataDefinitionService.get("productionScheduling", "orderOperationComponent");
        orderOperationComponentDD.save(operationComponent);

        operationTime += pathTime;
        if (parent != null && getIntegerValue(parent.getField("operationOffSet")).compareTo(operationTime) < 0) {
            parent.setField("operationOffSet", operationTime);
            orderOperationComponentDD.save(parent);
        }

        for (EntityTreeNode child : operationComponent.getChildren()) {
            int tmpPathTime = estimateRealizationTimeForOperation(child, plannedQuantity, operationTime, operationComponent);
            if (tmpPathTime > operationTime) {
                operationTime = tmpPathTime;
            }
        }

        return operationTime;
    }

    private Integer getIntegerValue(final Object value) {
        return value != null ? (Integer) value : Integer.valueOf(0);
    }

    private BigDecimal getBigDecimalFromField(final Object value, final Locale locale) {
        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);
            return new BigDecimal(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
