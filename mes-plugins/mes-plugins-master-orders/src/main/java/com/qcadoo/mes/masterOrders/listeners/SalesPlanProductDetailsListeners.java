package com.qcadoo.mes.masterOrders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.util.AdditionalUnitService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SalesPlanProductDetailsListeners {

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private AdditionalUnitService additionalUnitService;

    public void fillDefaultTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(SalesPlanProductFields.PRODUCT);
        Entity product = productLookup.getEntity();
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(SalesPlanProductFields.TECHNOLOGY);
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

    public void onPlannedQuantityChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        if (!additionalUnitService.isValidDecimalFieldWithoutMsg(view,
                Lists.newArrayList(SalesPlanProductFields.PLANNED_QUANTITY, SalesPlanProductFields.ORDERED_QUANTITY))) {
            return;
        }
        Entity salesPlanProduct = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntity();
        FieldComponent surplusFromPlan = (FieldComponent) view.getComponentByReference(SalesPlanProductFields.SURPLUS_FROM_PLAN);
        surplusFromPlan.setFieldValue(salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY)
                .subtract(salesPlanProduct.getDecimalField(SalesPlanProductFields.ORDERED_QUANTITY)));
        surplusFromPlan.requestComponentUpdateState();
    }
}
