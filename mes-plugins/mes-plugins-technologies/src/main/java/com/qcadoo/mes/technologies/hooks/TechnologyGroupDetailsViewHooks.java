package com.qcadoo.mes.technologies.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyGroupDetailsViewHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addTechnologyGroupToProduct(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent technologyGroupForm = (FormComponent) view.getComponentByReference("form");
        Entity technologyGroup = technologyGroupForm.getEntity();

        if (technologyGroup.getId() == null) {
            return;
        }

        FormComponent productForm = (FormComponent) view.getComponentByReference("product");
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        product = getProductFromDB(product.getId());

        product.setField("technologyGroup", technologyGroup);
        product.getDataDefinition().save(product);
    }

    private Entity getProductFromDB(final Long productId) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
    }
}
