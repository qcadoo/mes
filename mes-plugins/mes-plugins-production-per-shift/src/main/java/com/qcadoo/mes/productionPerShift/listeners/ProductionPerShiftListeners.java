package com.qcadoo.mes.productionPerShift.listeners;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

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

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", orderId);

        String url = "../page/productionPerShift/productionPerShiftView.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    public void fillProducedField(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        ComponentState operationLookup = viewState.getComponentByReference("productionPerShiftOperation");
        Long id = (Long) operationLookup.getFieldValue();

        Entity tioc = dataDefinitionService.get("technologies", "technologyInstanceOperationComponent").get(id);

        Entity toc = tioc.getBelongsToField("technologyOperationComponent");

        Entity prodComp = technologyService.getMainOutputProductComponent(toc);
        Entity prod = prodComp.getBelongsToField("product");

        ComponentState producesInput = viewState.getComponentByReference("produces");
        producesInput.setFieldValue(prod.getStringField("name"));

    }
}
