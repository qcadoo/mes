package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.cmmsMachineParts.constants.ProductFieldsCMP;
import com.qcadoo.mes.cmmsMachineParts.criteriaModifiers.ProductCriteriaModifiersCMP;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class ProductHooksCMP {

    private static final String L_FORM = "form";

    private static final String L_PARENT = "parent";

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        LookupComponent parentLookup = (LookupComponent) view.getComponentByReference(L_PARENT);
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        Entity product = form.getPersistedEntityWithIncludedFormValues();

        FilterValueHolder holder = parentLookup.getFilterValue();
        holder.put(ProductCriteriaModifiersCMP.MACHINE_PART_FILTER_PARAMETER, product.getBooleanField(ProductFieldsCMP.MACHINE_PART));
        parentLookup.setFilterValue(holder);
    }
}
