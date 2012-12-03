package com.qcadoo.mes.productionCounting.listeners;

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.ACCEPTED;
import static com.qcadoo.mes.orders.states.constants.OrderState.COMPLETED;
import static com.qcadoo.mes.orders.states.constants.OrderState.IN_PROGRESS;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.AUTO_CLOSE_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.LAST_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.ORDER;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.constants.RecordOperationProductInComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionRecordDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    private static final String L_FORM = "form";

    public void copyPlannedQuantity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        Entity productionRecord = form.getEntity().getDataDefinition().get(form.getEntityId());
        copyQuantity(productionRecord.getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS));
        copyQuantity(productionRecord.getHasManyField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS));
    }

    private void copyQuantity(List<Entity> records) {
        for (Entity record : records) {
            BigDecimal plannedQuantity = record.getDecimalField(RecordOperationProductInComponentFields.PLANNED_QUANTITY);
            if (plannedQuantity == null) {
                plannedQuantity = BigDecimal.ZERO;
            }
            record.setField(RecordOperationProductInComponentFields.USED_QUANTITY, plannedQuantity);
            record.getDataDefinition().save(record);
        }
    }

    public void closeOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity order = getOrderFromLookup(view);

        if (order == null) {
            return;
        }

        Boolean autoCloseOrder = order.getBooleanField(AUTO_CLOSE_ORDER);
        String orderState = order.getStringField(STATE);
        if (autoCloseOrder && "1".equals(view.getComponentByReference(LAST_RECORD).getFieldValue())
                && view.getComponentByReference(STATE).getFieldValue().equals(ACCEPTED.getStringValue())
                && IN_PROGRESS.getStringValue().equals(orderState)) {
            order.setField(STATE, COMPLETED.getStringValue());
            dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).save(order);
            Entity orderFromDB = order.getDataDefinition().get(order.getId());
            if (orderFromDB.getStringField(STATE).equals(COMPLETED.getStringValue())) {
                form.addMessage("productionCounting.order.orderClosed", MessageType.INFO, false);
            } else {
                form.addMessage("productionCounting.order.orderCannotBeClosed", MessageType.INFO, false);

                List<ErrorMessage> errors = Lists.newArrayList();
                if (!order.getErrors().isEmpty()) {
                    errors.addAll(order.getErrors().values());
                }
                if (!order.getGlobalErrors().isEmpty()) {
                    errors.addAll(order.getGlobalErrors());
                }

                StringBuilder errorMessages = new StringBuilder();
                for (ErrorMessage message : errors) {
                    String translatedErrorMessage = translationService.translate(message.getMessage(), view.getLocale(),
                            message.getVars());
                    errorMessages.append(translatedErrorMessage);
                    errorMessages.append(", ");
                }
                form.addMessage("orders.order.orderStates.error", MessageType.FAILURE, false, errorMessages.toString());

            }
        }
    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference(ORDER);
        return lookup.getEntity();
    }

}
