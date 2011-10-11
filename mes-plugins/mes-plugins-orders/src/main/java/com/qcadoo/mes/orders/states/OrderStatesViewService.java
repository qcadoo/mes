package com.qcadoo.mes.orders.states;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class OrderStatesViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PluginAccessor pluginAccessor;

    private void changeOrderStateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final OrderStates oldState, final OrderStates newState) {
        FieldComponent orderState = (FieldComponent) viewDefinitionState.getComponentByReference("state");
        orderState.setFieldValue(newState.getStringValue());

        FieldComponent externalSynchronized = (FieldComponent) viewDefinitionState
                .getComponentByReference("externalSynchronized");
        FieldComponent externalNumber = (FieldComponent) viewDefinitionState.getComponentByReference("externalNumber");

        if (hasIntegrationWithExternalSystem() && StringUtils.hasText((String) externalNumber.getFieldValue())) {
            externalSynchronized.setFieldValue("false");
        } else {
            externalSynchronized.setFieldValue("true");
        }

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();

        if (newState.getStringValue().equals(OrderStates.COMPLETED)) {
            if (checkAutogenealogyRequired() && !checkRequiredBatch(order)) {
                state.addMessage(translationService.translate("genealogies.message.batchNotFound", state.getLocale()),
                        MessageType.FAILURE);
                return;
            }
            if (isQualityControlAutoCheckEnabled() && !checkIfAllQualityControlsAreClosed(order)) {
                state.addMessage(translationService.translate("qualityControls.qualityControls.not.closed", state.getLocale()),
                        MessageType.FAILURE);
                return;
            }
        }

        state.performEvent(viewDefinitionState, "save", new String[0]);
    }

    public void changeOrderStateToAccepted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.PENDING, OrderStates.ACCEPTED);
    }

    public void changeOrderStateToInProgress(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if ((OrderStates.ACCEPTED.getStringValue().equals(order.getStringField("state")))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.ACCEPTED, OrderStates.IN_PROGRESS);
        } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField("state"))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.INTERRUPTED, OrderStates.IN_PROGRESS);
        }
    }

    public void changeOrderStateToCompleted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.COMPLETED);
    }

    public void changeOrderStateToDeclined(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if ((OrderStates.ACCEPTED.getStringValue().equals(order.getStringField("state")))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.ACCEPTED, OrderStates.DECLINED);
        } else if (OrderStates.PENDING.getStringValue().equals(order.getStringField("state"))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.PENDING, OrderStates.DECLINED);
        }
    }

    public void changeOrderStateToAbandoned(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity();
        if ((OrderStates.IN_PROGRESS.getStringValue().equals(order.getStringField("state")))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.ABANDONED);
        } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField("state"))) {
            changeOrderStateTo(viewDefinitionState, state, OrderStates.INTERRUPTED, OrderStates.ABANDONED);
        }
    }

    public void changeOrderStateToInterrupted(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeOrderStateTo(viewDefinitionState, state, OrderStates.IN_PROGRESS, OrderStates.INTERRUPTED);
    }

    private void changeOrderStateToForGrid(final ComponentState state, final Long id, final OrderStates oldState,
            final OrderStates newState) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
        order.setField("state", newState.getStringValue());

        if (hasIntegrationWithExternalSystem() && StringUtils.hasText(order.getStringField("externalNumber"))) {
            order.setField("externalSynchronized", false);
        } else {
            order.setField("externalSynchronized", true);
        }

        if (newState.getStringValue().equals(OrderStates.COMPLETED)) {
            if (checkAutogenealogyRequired() && !checkRequiredBatch(order)) {
                state.addMessage(translationService.translate("genealogies.message.batchNotFound", state.getLocale()),
                        MessageType.FAILURE);
                return;
            }
            if (isQualityControlAutoCheckEnabled() && !checkIfAllQualityControlsAreClosed(order)) {
                state.addMessage(translationService.translate("qualityControls.qualityControls.not.closed", state.getLocale()),
                        MessageType.FAILURE);
                return;
            }
        }

        order.getDataDefinition().save(order);
    }

    public void changeOrderStateToAcceptedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(state, id, OrderStates.PENDING, OrderStates.ACCEPTED);
        }
    }

    public void changeOrderStateToInProgressForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if ((OrderStates.ACCEPTED.getStringValue().equals(order.getStringField("state")))) {
                changeOrderStateToForGrid(state, id, OrderStates.ACCEPTED, OrderStates.IN_PROGRESS);
            } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField("state"))) {
                changeOrderStateToForGrid(state, id, OrderStates.INTERRUPTED, OrderStates.IN_PROGRESS);
            }
        }
    }

    public void changeOrderStateToCompletedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(state, id, OrderStates.IN_PROGRESS, OrderStates.COMPLETED);
        }
    }

    public void changeOrderStateToDeclinedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if ((OrderStates.ACCEPTED.getStringValue().equals(order.getStringField("state")))) {
                changeOrderStateToForGrid(state, id, OrderStates.ACCEPTED, OrderStates.DECLINED);
            } else if (OrderStates.PENDING.getStringValue().equals(order.getStringField("state"))) {
                changeOrderStateToForGrid(state, id, OrderStates.PENDING, OrderStates.DECLINED);
            }
        }
    }

    public void changeOrderStateToAbandonedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            if ((OrderStates.IN_PROGRESS.getStringValue().equals(order.getStringField("state")))) {
                changeOrderStateToForGrid(state, id, OrderStates.IN_PROGRESS, OrderStates.ABANDONED);
            } else if (OrderStates.INTERRUPTED.getStringValue().equals(order.getStringField("state"))) {
                changeOrderStateToForGrid(state, id, OrderStates.INTERRUPTED, OrderStates.ABANDONED);
            }
        }
    }

    public void changeOrderStateToInterruptedForGrid(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        for (Long id : grid.getSelectedEntitiesIds()) {
            changeOrderStateToForGrid(state, id, OrderStates.IN_PROGRESS, OrderStates.INTERRUPTED);
        }
    }

    private boolean hasIntegrationWithExternalSystem() {
        Plugin plugin = pluginAccessor.getPlugin("mesPluginsIntegrationErp");
        return plugin != null && plugin.getState().equals(PluginState.ENABLED);
    }

    private boolean checkAutogenealogyRequired() {
        SearchResult searchResult = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)
                .find().setMaxResults(1).list();
        Entity parameter = null;
        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }
        if (parameter != null && parameter.getField("batchForDoneOrder") != null) {
            return !(parameter.getField("batchForDoneOrder").toString().equals("01none"));
        } else {
            return false;
        }
    }

    private boolean checkRequiredBatch(final Entity order) {
        Entity technology = (Entity) order.getField("technology");
        if (technology != null) {
            if (order.getHasManyField("genealogies").size() == 0) {
                if ((Boolean) technology.getField("batchRequired")) {
                    return false;
                }
                if ((Boolean) technology.getField("shiftFeatureRequired")) {
                    return false;
                }
                if ((Boolean) technology.getField("postFeatureRequired")) {
                    return false;
                }
                if ((Boolean) technology.getField("otherFeatureRequired")) {
                    return false;
                }
                for (Entity operationComponent : technology.getTreeField("operationComponents")) {
                    for (Entity operationProductComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                        if ((Boolean) operationProductComponent.getField("batchRequired")) {
                            return false;
                        }
                    }
                }
            }
            for (Entity genealogy : order.getHasManyField("genealogies")) {
                if ((Boolean) technology.getField("batchRequired") && genealogy.getField("batch") == null) {
                    return false;
                }
                if ((Boolean) technology.getField("shiftFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("shiftFeatures");
                    if (entityList.size() == 0) {
                        return false;
                    }
                }
                if ((Boolean) technology.getField("postFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("postFeatures");
                    if (entityList.size() == 0) {
                        return false;
                    }
                }
                if ((Boolean) technology.getField("otherFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("otherFeatures");
                    if (entityList.size() == 0) {
                        return false;
                    }
                }
                for (Entity genealogyProductIn : genealogy.getHasManyField("productInComponents")) {
                    if ((Boolean) (genealogyProductIn.getBelongsToField("productInComponent").getField("batchRequired"))) {
                        List<Entity> entityList = genealogyProductIn.getHasManyField("batch");
                        if (entityList.size() == 0) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isQualityControlAutoCheckEnabled() {
        SearchResult searchResult = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)
                .find().setMaxResults(1).list();

        Entity parameter = null;
        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }

        if (parameter != null) {
            return (Boolean) parameter.getField("checkDoneOrderForQuality");
        } else {
            return false;
        }
    }

    private boolean checkIfAllQualityControlsAreClosed(final Entity order) {
        if (order.getBelongsToField("technology") == null) {
            return true;
        }

        Object controlTypeField = order.getBelongsToField("technology").getField("qualityControlType");

        if (controlTypeField != null) {
            DataDefinition qualityControlDD = null;

            qualityControlDD = dataDefinitionService.get("qualityControls", "qualityControl");

            if (qualityControlDD != null) {
                SearchResult searchResult = qualityControlDD.find().belongsTo("order", order.getId()).isEq("closed", false)
                        .list();
                return (searchResult.getTotalNumberOfEntities() <= 0);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
