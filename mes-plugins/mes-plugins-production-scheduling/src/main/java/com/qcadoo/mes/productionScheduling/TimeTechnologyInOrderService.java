package com.qcadoo.mes.productionScheduling;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TimeTechnologyInOrderService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private TranslationService translationService;

    public void setVisibleAlert(final ViewDefinitionState viewDefinitionState) {
        // ieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        ComponentState alert = viewDefinitionState.getComponentByReference("alert");

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        if (form.getEntityId() == null) {
            // if (realizationTime == null || realizationTime.getFieldValue() == null /* ||
            // "0".equals(realizationTime.getFieldValue()) */) {
            alert.setVisible(true);
        } else {
            alert.setVisible(false);
        }
    }

    @Transactional
    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("quantity");
        FieldComponent startTime = (FieldComponent) viewDefinitionState.getComponentByReference("startTime");
        FieldComponent stopTime = (FieldComponent) viewDefinitionState.getComponentByReference("stopTime");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");

        if (plannedQuantity.getFieldValue() == null || startTime.getFieldValue() == null) {
            realizationTime.setFieldValue(null);
            stopTime.setFieldValue(null);
        }

        int maxPathTime = 0;

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());

        maxPathTime = orderRealizationTimeService.estimateRealizationTimeForOperation(
                order.getTreeField("orderOperationComponents").getRoot(),
                orderRealizationTimeService.getBigDecimalFromField(plannedQuantity.getFieldValue(),
                        viewDefinitionState.getLocale()));

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage(
                    translationService.translate("orders.validate.global.error.RealizationTimeIsToLong",
                            viewDefinitionState.getLocale()), MessageType.FAILURE);
            realizationTime.setFieldValue(null);
            stopTime.setFieldValue(null);
        } else {
            realizationTime.setFieldValue(maxPathTime);

            Date dateFrom = orderRealizationTimeService.getDateFromField(startTime.getFieldValue());
            Date dateTo = shiftsService.findDateToForOrder(dateFrom, maxPathTime);
            dateFrom = shiftsService.findDateFromForOrder(dateTo, maxPathTime);

            if (dateFrom != null) {
                startTime.setFieldValue(orderRealizationTimeService.setDateToField(dateFrom));
            } else {
                startTime.setFieldValue(null);
            }

            if (dateTo != null) {
                stopTime.setFieldValue(orderRealizationTimeService.setDateToField(dateTo));
            } else {
                stopTime.setFieldValue(null);
            }
        }
    }

    public void disableRealizationTime(final ViewDefinitionState viewDefinitionState) {
        viewDefinitionState.getComponentByReference("realizationTime").setEnabled(false);
    }

    public void clearFieldValue(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        state.setFieldValue(null);
    }

}
