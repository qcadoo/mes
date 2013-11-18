package com.qcadoo.mes.productionCounting.listeners;

import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.internal.ProductionRecordViewService;
import com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class ProductionRecordDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_STATE = "state";

    private static final String L_RECORD_OPERATION_PRODUCT_IN_COMPONENT = "recordOperationProductInComponent";

    private static final String L_RECORD_OPERATION_PRODUCT_OUT_COMPONENT = "recordOperationProductOutComponent";

    private static final Set<String> COMPONENTS_TO_DISABLE = Sets.newHashSet("lastRecord", "number", "order",
            "technologyInstanceOperationComponent", "staff", "shift", "workstationType", "division",
            "recordOperationProductInComponent", "recordOperationProductOutComponent", "laborTime", "machineTime",
            "executedOperationCycles");

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionRecordDetailsListeners.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionRecordViewService productionRecordViewService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            setStateFieldValueToDraft(view);
        } else {
            initializeRecordDetailsView(view);
            Entity productionRecord = getProductionRecordFromDb(form.getEntityId());
            showLastStateChangeFailNotification(form, productionRecord);
            toggleFormEditable(view, productionRecord);
            toggleCopyButtonEnable(view, productionRecord);
        }
    }

    private void toggleFormEditable(final ViewDefinitionState view, final Entity productionRecord) {
        boolean isExternalSynchronized = productionRecord.getBooleanField(ProductionRecordFields.IS_EXTERNAL_SYNCHRONIZED);
        boolean isDraft = ProductionRecordStateStringValues.DRAFT.equals(productionRecord
                .getStringField(ProductionRecordFields.STATE));
        for (String componentReferenceName : COMPONENTS_TO_DISABLE) {
            ComponentState component = view.getComponentByReference(componentReferenceName);
            if (component == null) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(String.format("Cannot find component with reference='%s'", componentReferenceName));
                }
            } else {
                component.setEnabled(isDraft && isExternalSynchronized);
            }
        }
    }

    private void showLastStateChangeFailNotification(final FormComponent form, final Entity productionRecord) {
        boolean lastStateChangeFails = productionRecord.getBooleanField(ProductionRecordFields.LAST_STATE_CHANGE_FAILS);
        if (lastStateChangeFails) {
            String lastStateChangeFailCause = productionRecord
                    .getStringField(ProductionRecordFields.LAST_STATE_CHANGE_FAIL_CAUSE);

            if (StringUtils.isEmpty(lastStateChangeFailCause)) {
                form.addMessage("productionCounting.productionRecord.info.lastStateChangeFails", ComponentState.MessageType.INFO,
                        true, lastStateChangeFailCause);
            } else {
                form.addMessage("productionCounting.productionRecord.info.lastStateChangeFails.withCause",
                        ComponentState.MessageType.INFO, false, lastStateChangeFailCause);
            }
        }
    }

    private Entity getProductionRecordFromDb(final Long entityId) {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_RECORD).get(entityId);
    }

    private void toggleCopyButtonEnable(final ViewDefinitionState view, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);
        String orderState = order.getStringField(OrderFields.STATE);
        Boolean orderIsInProgress = OrderState.IN_PROGRESS.getStringValue().equals(orderState);
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem copyButton = window.getRibbon().getGroupByName("actions").getItemByName("copy");
        copyButton.setEnabled(orderIsInProgress);
        copyButton.requestUpdate(true);
    }

    private void setStateFieldValueToDraft(final ViewDefinitionState view) {
        FieldComponent status = (FieldComponent) view.getComponentByReference(L_STATE);
        status.setFieldValue(ProductionRecordState.DRAFT.getStringValue());
        status.requestComponentUpdateState();
    }

    public void initializeRecordDetailsView(final ViewDefinitionState view) {
        Entity order = ((LookupComponent) view.getComponentByReference(ORDER)).getEntity();
        if (order == null) {
            return;
        }
        String recordingType = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(recordingType, order, view);

        view.getComponentByReference(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).setVisible(
                FOR_EACH.getStringValue().equals(recordingType));
        view.getComponentByReference(L_RECORD_OPERATION_PRODUCT_OUT_COMPONENT).setVisible(
                order.getBooleanField(REGISTER_QUANTITY_OUT_PRODUCT));
        view.getComponentByReference(L_RECORD_OPERATION_PRODUCT_IN_COMPONENT).setVisible(
                order.getBooleanField(REGISTER_QUANTITY_IN_PRODUCT));

        view.getComponentByReference("isDisabled").setFieldValue(false);
    }
}
