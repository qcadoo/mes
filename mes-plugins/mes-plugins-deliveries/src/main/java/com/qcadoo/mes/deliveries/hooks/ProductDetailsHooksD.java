package com.qcadoo.mes.deliveries.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductDetailsHooksD {

    public static final String L_PARENT_ID = "parenId";

    

    public void beforeRender(final ViewDefinitionState view) {
        toggleSuppliersGrids(view);
        updateParentCompaniesCriteriaModifiersState(view);
    }

    public void toggleSuppliersGrids(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = form.getPersistedEntityWithIncludedFormValues();
        GridComponent productCompanies = (GridComponent) view.getComponentByReference("productCompanies");
        GridComponent productsFamilyCompanies = (GridComponent) view.getComponentByReference("productsFamilyCompanies");
        if (ProductFamilyElementType.from(product).compareTo(ProductFamilyElementType.PARTICULAR_PRODUCT) == 0) {
            productCompanies.setVisible(true);
            productsFamilyCompanies.setVisible(false);
        } else {
            productCompanies.setVisible(false);
            productsFamilyCompanies.setVisible(true);
        }
    }

    private void updateParentCompaniesCriteriaModifiersState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent parentCompanies = (GridComponent) view.getComponentByReference("parentCompanies");

        Long parentId = null;
        Entity parent = form.getEntity().getBelongsToField(ProductFields.PARENT);
        if (parent != null) {
            parentId = parent.getId();
        }
        FilterValueHolder filterValueHolder = parentCompanies.getFilterValue();
        filterValueHolder.put(L_PARENT_ID, parentId);
        parentCompanies.setFilterValue(filterValueHolder);
    }
}
