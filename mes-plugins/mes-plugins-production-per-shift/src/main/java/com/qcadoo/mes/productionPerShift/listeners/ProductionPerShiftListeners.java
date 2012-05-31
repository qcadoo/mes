package com.qcadoo.mes.productionPerShift.listeners;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;

@Service
public class ProductionPerShiftListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

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

        String url = "../page/productionPerShift/productionPerShiftView.html";
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
        fillProducedField(viewState);
    }

    private void fillProducedField(final ViewDefinitionState viewState) {
        Entity tioc = getTiocFromOperationLookup(viewState);
        String producedProduct = null;

        if (tioc != null) {
            Entity toc = tioc.getBelongsToField("technologyOperationComponent");

            Entity prodComp = technologyService.getMainOutputProductComponent(toc);
            Entity prod = prodComp.getBelongsToField("product");
            producedProduct = prod.getStringField("name");
        }

        ComponentState producesInput = viewState.getComponentByReference("produces");
        producesInput.setFieldValue(producedProduct);
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
        Entity tioc = getTiocFromOperationLookup(viewState);

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
        Entity tioc = getTiocFromOperationLookup(viewState);
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
        fillProducedField(viewState);
        fillProgressForDays(viewState);
    }

    private Entity getTiocFromOperationLookup(final ViewDefinitionState viewState) {
        ComponentState operationLookup = viewState.getComponentByReference("productionPerShiftOperation");
        Long id = (Long) operationLookup.getFieldValue();
        Entity tioc = null;
        if (id != null) {
            tioc = dataDefinitionService.get("technologies", "technologyInstanceOperationComponent").get(id);
        }
        return tioc;
    }
}
