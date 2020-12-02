package com.qcadoo.mes.orders.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.SalesPlanProductFields;
import com.qcadoo.mes.orders.criteriaModifiers.ProductCriteriaModifiersO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesPlanProductDetailsHooks {

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUnit";

    private static final String L_ORDERED_QUANTITY_UNIT = "orderedQuantityUnit";

    private static final String L_SURPLUS_FROM_PLAN_UNIT = "surplusFromPlanUnit";

    public void onBeforeRender(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList(L_PLANNED_QUANTITY_UNIT, L_ORDERED_QUANTITY_UNIT,
                L_SURPLUS_FROM_PLAN_UNIT);

        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(SalesPlanProductFields.PRODUCT);
        Entity product = productLookup.getEntity();

        String unit = "";

        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);
        }

        for (String referenceName : referenceNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(referenceName);
            field.setFieldValue(unit);
            field.requestComponentUpdateState();
        }
        setCriteriaModifierParameters(productLookup,
                ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntity());
    }

    private void setCriteriaModifierParameters(LookupComponent productLookup, Entity salesPlanProduct) {
        FilterValueHolder filterValueHolder = productLookup.getFilterValue();

        Long salesPlanId = salesPlanProduct.getBelongsToField(SalesPlanProductFields.SALES_PLAN).getId();
        Entity product = salesPlanProduct.getBelongsToField(SalesPlanProductFields.PRODUCT);

        if (product != null) {
            filterValueHolder.put(ProductCriteriaModifiersO.L_PRODUCT_ID, product.getId());
        }

        filterValueHolder.put(ProductCriteriaModifiersO.L_SALES_PLAN_ID, salesPlanId);

        productLookup.setFilterValue(filterValueHolder);
        productLookup.requestComponentUpdateState();
    }
}
