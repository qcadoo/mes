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

    public static final String L_PARENT_ID = "parentId";

    public void beforeRender(final ViewDefinitionState view) {
        updateParentCompaniesCriteriaModifiersState(view);
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

        parentCompanies.setVisible(!ProductFamilyElementType.from(product).equals(ProductFamilyElementType.PRODUCTS_FAMILY));
    }

}
