package com.qcadoo.mes.productionPerShift.listeners;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionPerShift.hooks.ProductionPerShiftDetailsHooks;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionPerShiftListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionPerShiftDetailsHooks detailsHooks;

    public void redirectToProductionPerShift(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Long orderId = (Long) componentState.getFieldValue();

        if (orderId == null) {
            return;
        }

        long ppsId = createCorrespondingProductionPerShfitEntity(orderId);

        redirect(viewState, ppsId);
    }

    void redirect(final ViewDefinitionState viewState, final Long ppsId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", ppsId);

        String url = "../page/productionPerShift/productionPerShiftDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    private long createCorrespondingProductionPerShfitEntity(final Long orderId) {
        DataDefinition orderDD = dataDefinitionService.get("orders", "order");
        Entity order = orderDD.get(orderId);

        DataDefinition ppsDD = dataDefinitionService.get("productionPerShift", "productionPerShift");

        Entity pps = getPps(order, ppsDD);

        if (pps == null) {
            pps = ppsDD.create();
            pps.setField("order", order);
            ppsDD.save(pps);
        }

        return getPps(order, ppsDD).getId();
    }

    private Entity getPps(final Entity order, final DataDefinition ppsDD) {
        return ppsDD.find().add(SearchRestrictions.belongsTo("order", order)).uniqueResult();
    }

    public void fillProducedField(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        detailsHooks.fillProducedField(viewState);
    }

    /**
     * Fill outer AwesomeDynamicList with entities fetched from db. Disable ADL if operation lookup is empty.
     * 
     * @param viewState
     * @param componentState
     * @param args
     */
    public void fillProgressForDays(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        fillProgressForDays(viewState);
    }

    private void fillProgressForDays(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference("progressForDaysADL");
        Entity tioc = detailsHooks.getTiocFromOperationLookup(viewState);

        if (tioc == null) {
            progressForDaysADL.setFieldValue(null);
            progressForDaysADL.setEnabled(false);
        } else {
            List<Entity> progressForDays = tioc.getHasManyField("progressForDay");
            progressForDaysADL.setFieldValue(progressForDays);
            progressForDaysADL.setEnabled(true);
        }
    }

    /**
     * Save outer AwesomeDynamicList entities in db and reset operation lookup & related components
     * 
     * @param viewState
     * @param componentState
     * @param args
     */
    public void saveProgressForDays(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference("progressForDaysADL");
        @SuppressWarnings("unchecked")
        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();
        Entity tioc = detailsHooks.getTiocFromOperationLookup(viewState);
        if (tioc != null) {
            tioc.setField("progressForDay", progressForDays);
            tioc.getDataDefinition().save(tioc);
        }
        resetProgressForDaysComponents(viewState);
    }

    private void resetProgressForDaysComponents(final ViewDefinitionState viewState) {
        ComponentState operationLookup = viewState.getComponentByReference("productionPerShiftOperation");
        operationLookup.setFieldValue(null);
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference("progressForDaysADL");
        progressForDaysADL.setFieldValue(null);
        detailsHooks.fillProducedField(viewState);
        fillProgressForDays(viewState);
    }

    public void addPlannedProgresTypeForTechInstOperComp(final ViewDefinitionState viewState,
            final ComponentState componentState, final String[] args) {
        Entity operation = getOperationFromLookup(viewState);
        String plannedProgressType = ((FieldComponent) viewState.getComponentByReference("plannedProgressType")).getFieldValue()
                .toString();
        operation.setField("plannedProgressType", plannedProgressType);
        operation.getDataDefinition().save(operation);
    }

    public void changeView(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        detailsHooks.disablePlannedProgressTypeForPendingOrder(viewState);
        detailsHooks.disableReasonOfCorrection(viewState);
    }

    public void fillADL(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        DataDefinition ppsDD = dataDefinitionService.get("productionPerShift", "productionPerShift");
        Entity pps = getPps(detailsHooks.getOrderFromLookup(viewState), ppsDD);
    }

    private Entity getOperationFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference("productionPerShiftOperation");
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION).get(
                (Long) lookup.getFieldValue());
    }

}
