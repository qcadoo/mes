package com.qcadoo.mes.basic.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ModelFields;
import com.qcadoo.mes.basic.criteriaModifiers.ProductCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ModelDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        fillCriteriaModifiers(view);
    }

    private void fillCriteriaModifiers(final ViewDefinitionState view) {
        FormComponent modelForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent assortmentLookup = (LookupComponent) view.getComponentByReference(ModelFields.ASSORTMENT);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference("productLookup");

        Long modelId = modelForm.getEntityId();
        Entity assortment = assortmentLookup.getEntity();

        FilterValueHolder filterValueHolder = productLookup.getFilterValue();

        if (Objects.nonNull(modelId)) {
            filterValueHolder.put(ProductCriteriaModifiers.L_MODEL_ID, modelId);
        }
        if (Objects.isNull(assortment)) {
            if (filterValueHolder.has(ProductCriteriaModifiers.L_ASSORTMENT_ID)) {
                filterValueHolder.remove(ProductCriteriaModifiers.L_ASSORTMENT_ID);
            }
        } else {
            filterValueHolder.put(ProductCriteriaModifiers.L_ASSORTMENT_ID, assortment.getId());
        }

        productLookup.setFilterValue(filterValueHolder);
    }

}
