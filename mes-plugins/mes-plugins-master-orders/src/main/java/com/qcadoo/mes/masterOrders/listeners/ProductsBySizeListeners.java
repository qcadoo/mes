package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.ProductsBySizeHelperFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductsBySizeListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changeFamilyProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference("product");
        Entity product = productField.getEntity();
        if (Objects.nonNull(product)) {
            clearProductsList(view, product);
            prepareProductsList(view, product);
        } else {
            clearProductsList(view, product);
        }
    }

    private void prepareProductsList(ViewDefinitionState view, Entity product) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        List<Entity> childs = product.getHasManyField(ProductFields.PRODUCT_FAMILY_CHILDRENS);
        for (Entity child : childs) {
            if (Objects.nonNull(child.getBelongsToField(ProductFields.SIZE))) {
                Entity entry = dataDefinitionService
                        .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_ENTRY_HELPER)
                        .create();
                entry.setField("product", child.getId());
                entry.setField("productsBySizeHelper", form.getEntityId());
                entry.getDataDefinition().save(entry);
            }
        }
    }

    private void clearProductsList(ViewDefinitionState view, Entity product) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity helper = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_HELPER)
                .get(form.getEntityId());
        for (Entity entity : helper.getHasManyField(ProductsBySizeHelperFields.PRODUCTS_BY_SIZE_ENTRY_HELPERS)) {
            entity.getDataDefinition().delete(entity.getId());
        }
    }

    public void addPositionsToOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }
}
