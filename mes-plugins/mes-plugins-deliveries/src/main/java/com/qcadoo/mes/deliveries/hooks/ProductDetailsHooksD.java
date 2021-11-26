package com.qcadoo.mes.deliveries.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.ProductFieldsD;
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
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getPersistedEntityWithIncludedFormValues();

        GridComponent productCompanies = (GridComponent) view.getComponentByReference(ProductFieldsD.PRODUCT_COMPANIES);
        GridComponent productsFamilyCompanies = (GridComponent) view
                .getComponentByReference(ProductFieldsD.PRODUCTS_FAMILY_COMPANIES);

        if (ProductFamilyElementType.from(product).equals(ProductFamilyElementType.PARTICULAR_PRODUCT)) {
            productCompanies.setVisible(true);
            productsFamilyCompanies.setVisible(false);
        } else {
            productCompanies.setVisible(false);
            productsFamilyCompanies.setVisible(true);
        }
    }

    private void updateParentCompaniesCriteriaModifiersState(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent parentCompanies = (GridComponent) view.getComponentByReference("parentCompanies");

        Entity product = productForm.getEntity().getDataDefinition().get(productForm.getEntityId());
        Entity parent = product.getBelongsToField(ProductFields.PARENT);

        Long parentId = null;

        if (Objects.nonNull(parent)) {
            parentId = parent.getId();
        }

        FilterValueHolder filterValueHolder = parentCompanies.getFilterValue();
        filterValueHolder.put(L_PARENT_ID, parentId);

        parentCompanies.setFilterValue(filterValueHolder);
    }

}
