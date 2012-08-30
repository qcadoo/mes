package com.qcadoo.mes.basic.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductFamiliesAddProductListeners {

    public final void addSelectedProductToFamily(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity formEntity = form.getEntity();
        LookupComponent childLookup = (LookupComponent) view.getComponentByReference("child");
        Entity child = childLookup.getEntity();
        if (child == null) {
            return;
        } else {
            child.setField(ProductFields.PARENT, formEntity.getField(ProductFields.PARENT));
            child.getDataDefinition().save(child);
        }
    }

}
