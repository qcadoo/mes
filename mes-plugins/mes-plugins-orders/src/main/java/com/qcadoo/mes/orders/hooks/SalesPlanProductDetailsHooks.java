package com.qcadoo.mes.orders.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.SalesPlanProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesPlanProductDetailsHooks {

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUnit";

    private static final String L_ORDERED_QUANTITY_UNIT = "orderedQuantityUnit";

    private static final String L_SURPLUS_FROM_PLAN_UNIT = "surplusFromPlanUnit";

    @Autowired
    private TechnologyServiceO technologyServiceO;

    public void onBeforeRender(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList(L_PLANNED_QUANTITY_UNIT, L_ORDERED_QUANTITY_UNIT,
                L_SURPLUS_FROM_PLAN_UNIT);

        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(SalesPlanProductFields.PRODUCT);
        Entity product = productLookup.getEntity();

        String unit = "";

        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);

            LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(SalesPlanProductFields.TECHNOLOGY);
            if (technologyLookup.getEntity() == null) {
                Entity defaultTechnology = technologyServiceO.getDefaultTechnology(product);

                if (defaultTechnology != null) {
                    technologyLookup.setFieldValue(defaultTechnology);
                    technologyLookup.requestComponentUpdateState();
                } else {
                    Entity productFamily = product.getBelongsToField(ProductFields.PARENT);
                    if (productFamily != null) {
                        defaultTechnology = technologyServiceO.getDefaultTechnology(productFamily);
                        if (defaultTechnology != null) {
                            technologyLookup.setFieldValue(defaultTechnology);
                            technologyLookup.requestComponentUpdateState();
                        }
                    }
                }
            }
        }

        for (String referenceName : referenceNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(referenceName);
            field.setFieldValue(unit);
            field.requestComponentUpdateState();
        }

    }
}
