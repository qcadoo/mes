package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.CostNormsGeneratorFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class CostNormsGeneratorHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference("productsLookup");
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity generator = form.getEntity();
        String costSource = generator.getStringField(CostNormsGeneratorFields.COSTS_SOURCE);
        FilterValueHolder filterValueHolder = productLookup.getFilterValue();
        filterValueHolder.put("costSource", costSource);
        productLookup.setFilterValue(filterValueHolder);

    }
}
