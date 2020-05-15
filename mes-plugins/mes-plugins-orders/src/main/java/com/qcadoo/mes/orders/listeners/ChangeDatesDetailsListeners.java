package com.qcadoo.mes.orders.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.ChangeDatesHelperFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChangeDatesDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changeDates(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity helper = form.getEntity();
        String orderIds = helper.getStringField(ChangeDatesHelperFields.ORDER_IDS);
        List<Long> ids = Lists.newArrayList(orderIds.split(",")).stream().map(Long::valueOf).collect(Collectors.toList());
        Date dateFrom = helper.getDateField(ChangeDatesHelperFields.DATE_FROM);
        Date dateTo = helper.getDateField(ChangeDatesHelperFields.DATE_TO);
        String commentDateFrom = helper.getStringField(ChangeDatesHelperFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM);
        String commentDateTo = helper.getStringField(ChangeDatesHelperFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO);
        List<Entity> reasonsDateFrom = helper.getHasManyField(ChangeDatesHelperFields.REASON_TYPES_CORRECTION_DATE_FROM);
        List<Entity> reasonsDateTo = helper.getHasManyField(ChangeDatesHelperFields.REASON_TYPES_CORRECTION_DATE_TO);

        if (dateFrom == null && dateTo == null) {
            view.addMessage("orders.changeDatesHelper.error.datesMissing", ComponentState.MessageType.FAILURE);
            return;
        }
        List<String> notValidOrders = Lists.newArrayList();
        List<String> validOrders = Lists.newArrayList();
        for (Long id : ids) {
            Entity order = orderDD.get(id);
            if (validateDates(order, dateFrom, dateTo)) {
                copyFieldsToOrder(dateFrom, dateTo, commentDateFrom, commentDateTo, reasonsDateFrom, reasonsDateTo, order);

                Entity saved = orderDD.save(order);
                if (!saved.isValid()) {
                    notValidOrders.add(order.getStringField(OrderFields.NUMBER));
                } else {
                    validOrders.add(order.getStringField(OrderFields.NUMBER));
                }
            } else {
                notValidOrders.add(order.getStringField(OrderFields.NUMBER));
            }
        }
        if (!notValidOrders.isEmpty()) {
            view.addMessage("orders.changeDatesHelper.error.validationErrors", ComponentState.MessageType.INFO,
                    String.join(", ", notValidOrders));
        }

        if (!validOrders.isEmpty()) {
            view.addMessage("orders.changeDatesHelper.info.changeOrders", ComponentState.MessageType.INFO,
                    String.join(", ", validOrders));
        }
    }

    private void copyFieldsToOrder(Date dateFrom, Date dateTo, String commentDateFrom, String commentDateTo,
            List<Entity> reasonsDateFrom, List<Entity> reasonsDateTo, Entity order) {
        String orderState = order.getStringField(OrderFields.STATE);
        if (dateFrom != null) {
            order.setField(OrderFields.START_DATE, dateFrom);
            if (OrderState.ACCEPTED.getStringValue().equals(orderState)) {
                fillCommentAndReasons(commentDateFrom, reasonsDateFrom, order,
                        OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM, OrderFields.REASON_TYPES_CORRECTION_DATE_FROM);
            } else if (OrderState.IN_PROGRESS.getStringValue().equals(orderState)) {
                fillCommentAndReasons(commentDateFrom, reasonsDateFrom, order,
                        OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_START,
                        OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START);
            }
        }
        if (dateTo != null) {
            order.setField(OrderFields.FINISH_DATE, dateTo);
            if (OrderState.ACCEPTED.getStringValue().equals(orderState)
                    || OrderState.IN_PROGRESS.getStringValue().equals(orderState)) {
                fillCommentAndReasons(commentDateTo, reasonsDateTo, order, OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO,
                        OrderFields.REASON_TYPES_CORRECTION_DATE_TO);
            }
        }

    }

    private void fillCommentAndReasons(String commentDateFrom, List<Entity> reasonsDateFrom, Entity order,
            String commentReasonTypeCorrectionDateFrom, String reasonTypesCorrectionDateFrom) {
        order.setField(commentReasonTypeCorrectionDateFrom, commentDateFrom);
        if (!reasonsDateFrom.isEmpty()) {
            order.setField(reasonTypesCorrectionDateFrom, reasonsDateFrom);
        }
    }

    private boolean validateDates(final Entity order, final Date dateFrom, final Date dateTo) {
        Date originalDateFrom = order.getDateField(OrderFields.START_DATE);
        Date originalDateTo = order.getDateField(OrderFields.FINISH_DATE);
        if (dateFrom != null) {
            originalDateFrom = dateFrom;
        }
        if (dateTo != null) {
            originalDateTo = dateTo;
        }

        return originalDateFrom == null || originalDateTo == null || !originalDateFrom.after(originalDateTo);
    }
}
