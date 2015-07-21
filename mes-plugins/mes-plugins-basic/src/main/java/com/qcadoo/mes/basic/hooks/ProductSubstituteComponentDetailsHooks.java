package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SubstituteFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductSubstituteComponentDetailsHooks {

    public void fillFilterValues(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity productSubstitute = form.getPersistedEntityWithIncludedFormValues();
        Entity product = productSubstitute.getBelongsToField(SubstituteFields.BASE_PRODUCT);
        if (product != null) {
            LookupComponent productLookup = (LookupComponent) view.getComponentByReference(SubstituteFields.PRODUCT);
            FilterValueHolder filter = productLookup.getFilterValue();
            filter.put(SubstituteFields.PRODUCT, product.getId());
            productLookup.setFilterValue(filter);
        }
    }
}
