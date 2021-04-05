package com.qcadoo.mes.productFlowThruDivision.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.productFlowThruDivision.constants.ModelCardProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ModelCardProductDetailsListeners {

    @Autowired
    private TechnologyServiceO technologyServiceO;

    public void fillDefaultTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ModelCardProductFields.PRODUCT);
        Entity product = productLookup.getEntity();
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(ModelCardProductFields.TECHNOLOGY);
        if (product != null) {
            Entity defaultTechnology = technologyServiceO.getDefaultTechnology(product);
            if (defaultTechnology != null) {
                technologyLookup.setFieldValue(defaultTechnology.getId());
            } else {
                Entity productFamily = product.getBelongsToField(ProductFields.PARENT);
                if (productFamily != null) {
                    defaultTechnology = technologyServiceO.getDefaultTechnology(productFamily);
                    if (defaultTechnology != null) {
                        technologyLookup.setFieldValue(defaultTechnology.getId());
                    } else {
                        technologyLookup.setFieldValue(null);
                    }
                } else {
                    technologyLookup.setFieldValue(null);
                }
            }
        } else {
            technologyLookup.setFieldValue(null);
        }
        technologyLookup.requestComponentUpdateState();
    }
}
