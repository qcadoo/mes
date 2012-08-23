package com.qcadoo.mes.basic.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductFamiliesAddProductListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void addSelectedProductToFamily(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity formEntity = form.getEntity();
        Entity child = getProductFromLookup(view, "child");
        if (child != null) {
            child.setField(ProductFields.PARENT, formEntity.getField(ProductFields.PARENT));
        }
        child.getDataDefinition().save(child);
    }

    private Entity getProductFromLookup(final ViewDefinitionState view, final String fieldName) {
        ComponentState lookup = view.getComponentByReference(fieldName);
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                (Long) lookup.getFieldValue());
    }
}
