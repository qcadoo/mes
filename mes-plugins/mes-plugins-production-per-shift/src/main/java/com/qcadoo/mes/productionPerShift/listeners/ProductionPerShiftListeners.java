package com.qcadoo.mes.productionPerShift.listeners;

import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants.PLUGIN_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.hooks.ProductionPerShiftDetailsHooks;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
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
        detailsHooks.fillProgressForDays(viewState);
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
        String plannedProgressType = viewState.getComponentByReference("plannedProgressType").getFieldValue().toString();
        for (Entity progressForDay : progressForDays) {
            progressForDay.setField("corrected", plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
        }
        Entity tioc = detailsHooks.getTiocFromOperationLookup(viewState);
        boolean hasCorrections = detailsHooks.shouldHasCorrections(viewState);
        if (tioc != null) {
            tioc.setField("hasCorrections", hasCorrections);
            tioc.setField("progressForDays", prepareProgressForDaysForTIOC(tioc, hasCorrections, progressForDays));
            tioc = tioc.getDataDefinition().save(tioc);
            if (!tioc.isValid()) {
                List<ErrorMessage> errors = tioc.getGlobalErrors();
                for (ErrorMessage error : errors) {
                    componentState.addMessage(error.getMessage(), MessageType.FAILURE, error.getVars());
                }
            }
        }
    }

    private List<Entity> prepareProgressForDaysForTIOC(final Entity tioc, final boolean hasCorrections,
            final List<Entity> progressForDays) {
        Entity techInstOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(tioc.getId());
        List<Entity> plannedPrograssForDay = new ArrayList<Entity>();
        plannedPrograssForDay = techInstOperComp.getHasManyField("progressForDays").find()
                .add(SearchRestrictions.eq("corrected", !hasCorrections)).list().getEntities();
        plannedPrograssForDay.addAll(progressForDays);
        return plannedPrograssForDay;
    }

    private List<Entity> addCorrectedToPlannedProgressForDay(final Entity tioc, final List<Entity> progressForDays) {
        Entity techInstOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(tioc.getId());
        List<Entity> plannedPrograssForDay = techInstOperComp.getHasManyField("progressForDays").find().list().getEntities();
        plannedPrograssForDay.addAll(progressForDays);
        return plannedPrograssForDay;
    }

    public void changeView(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        detailsHooks.disablePlannedProgressTypeForPendingOrder(viewState);
        detailsHooks.disableReasonOfCorrection(viewState);
    }

    public void copyFromPlanned(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        DataDefinition progressForDayDD = dataDefinitionService.get(PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
        Entity tioc = detailsHooks.getTiocFromOperationLookup(viewState);
        if (tioc == null) {
            return;
        } else {
            String plannedProgressType = ((FieldComponent) viewState.getComponentByReference("plannedProgressType"))
                    .getFieldValue().toString();
            List<Entity> progressForDays = getProgressForDayFromTIOC(tioc,
                    plannedProgressType.equals(PlannedProgressType.PLANNED.getStringValue()));
            List<Entity> copiedProgressForDays = new ArrayList<Entity>();
            for (Entity progressForDay : progressForDays) {
                Entity copyProgressForDay = progressForDayDD.copy(progressForDay.getId()).get(0);
                copyProgressForDay.setField("corrected", true);
                copiedProgressForDays.add(copyProgressForDay);
            }
            boolean hasCorrection = detailsHooks.shouldHasCorrections(viewState);
            tioc.setField("hasCorrections", hasCorrection);
            tioc.setField(
                    "progressForDays",
                    addCorrectedToPlannedProgressForDay(tioc,
                            prepareProgressForDaysForTIOC(tioc, hasCorrection, copiedProgressForDays)));
            tioc = tioc.getDataDefinition().save(tioc);
        }
    }

    public void deleteProgressForDays(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        DataDefinition progressForDayDD = dataDefinitionService.get(PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
        Entity tioc = detailsHooks.getTiocFromOperationLookup(viewState);
        if (tioc == null) {
            return;
        } else {
            String plannedProgressType = ((FieldComponent) viewState.getComponentByReference("plannedProgressType"))
                    .getFieldValue().toString();
            List<Entity> progressForDays = getProgressForDayFromTIOC(tioc,
                    plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
            for (Entity progressForDay : progressForDays) {
                progressForDayDD.delete(progressForDay.getId());
            }
            tioc = tioc.getDataDefinition().save(tioc);
        }
    }

    private List<Entity> getProgressForDayFromTIOC(final Entity tioc, final boolean corrected) {
        return tioc.getHasManyField("progressForDays").find().add(SearchRestrictions.eq("corrected", corrected)).list()
                .getEntities();
    }
}
