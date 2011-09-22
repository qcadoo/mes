package com.qcadoo.mes.productionCounting.internal;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class RecordInAndOutService {

    public void fillFieldFromProduct(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        Entity record = ((FormComponent) view.getComponentByReference("form")).getEntity();
        Entity product = record.getBelongsToField("product");

        view.getComponentByReference("number").setFieldValue(product.getField("number"));
        view.getComponentByReference("number").setEnabled(false);
        view.getComponentByReference("name").setFieldValue(product.getField("name"));
        view.getComponentByReference("name").setEnabled(false);
        view.getComponentByReference("typeOfMaterial").setFieldValue(product.getField("typeOfMaterial"));
        view.getComponentByReference("typeOfMaterial").setEnabled(false);
    }
}
