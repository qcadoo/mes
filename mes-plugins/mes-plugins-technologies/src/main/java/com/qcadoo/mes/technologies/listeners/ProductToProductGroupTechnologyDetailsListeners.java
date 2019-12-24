package com.qcadoo.mes.technologies.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.hooks.ProductToProductGroupTechnologyDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductToProductGroupTechnologyDetailsListeners {

    @Autowired
    private ProductToProductGroupTechnologyDetailsHooks productToProductGroupTechnologyDetailsHooks;

    public void setCriteriaModifierParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productToProductGroupTechnologyDetailsHooks.toggleOrderProductEditable(view);

        productToProductGroupTechnologyDetailsHooks.setCriteriaModifierParameters(view);
    }
}
