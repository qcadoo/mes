package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.CostNormsGeneratorFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CostNormsGeneratorHooks {

	public void onBeforeRender(final ViewDefinitionState view) {
		FormComponent costNormsGeneratorForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
		LookupComponent productLookup = (LookupComponent) view.getComponentByReference("productsLookup");

		Entity costNormsGenerator = costNormsGeneratorForm.getEntity();

		String costsSource = costNormsGenerator.getStringField(CostNormsGeneratorFields.COSTS_SOURCE);

		FilterValueHolder filterValueHolder = productLookup.getFilterValue();
		filterValueHolder.put("costsSource", costsSource);

		productLookup.setFilterValue(filterValueHolder);
	}

}
