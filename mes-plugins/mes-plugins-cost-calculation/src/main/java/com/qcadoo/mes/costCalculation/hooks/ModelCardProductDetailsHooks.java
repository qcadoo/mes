package com.qcadoo.mes.costCalculation.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costCalculation.constants.ModelCardProductFields;
import com.qcadoo.mes.costCalculation.criteriaModifier.ProductCriteriaModifiersCC;
import com.qcadoo.mes.orders.criteriaModifiers.TechnologyCriteriaModifiersO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ModelCardProductDetailsHooks {

    private static final String L_QUANTITY_UNIT = "quantityUnit";

    public void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ModelCardProductFields.PRODUCT);
        FilterValueHolder filterValueHolder = productLookup.getFilterValue();
        Entity product = productLookup.getEntity();

        String unit = "";

        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);
            LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(ModelCardProductFields.TECHNOLOGY);
            FilterValueHolder technologyFilterValueHolder = technologyLookup.getFilterValue();
            technologyFilterValueHolder.put(TechnologyCriteriaModifiersO.PRODUCT_PARAMETER, product.getId());
            technologyLookup.setFilterValue(technologyFilterValueHolder);
        }

        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference(L_QUANTITY_UNIT);
        quantityUnit.setFieldValue(unit);
        quantityUnit.requestComponentUpdateState();

        Entity modelCardProduct = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntity();
        Long modelCardId = modelCardProduct.getBelongsToField(ModelCardProductFields.MODEL_CARD).getId();
        filterValueHolder.put(ProductCriteriaModifiersCC.L_MODEL_CARD_ID, modelCardId);

        Entity productFromDb = modelCardProduct.getBelongsToField(ModelCardProductFields.PRODUCT);
        if (productFromDb != null) {
            filterValueHolder.put(ProductCriteriaModifiersCC.L_PRODUCT_ID, productFromDb.getId());
        }

        productLookup.setFilterValue(filterValueHolder);
    }
}
