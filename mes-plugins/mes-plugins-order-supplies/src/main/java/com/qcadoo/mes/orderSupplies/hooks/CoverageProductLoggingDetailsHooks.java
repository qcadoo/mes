package com.qcadoo.mes.orderSupplies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductGeneratedFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CoverageProductLoggingDetailsHooks {

    

    public void updateCriteriaModifiersState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference("company");

        FilterValueHolder filterValueHolder = companyLookup.getFilterValue();
        filterValueHolder.put(CoverageProductGeneratedFields.PRODUCT_ID,
                form.getPersistedEntityWithIncludedFormValues().getBelongsToField(CoverageProductFields.PRODUCT).getId());
        companyLookup.setFilterValue(filterValueHolder);
    }
}
