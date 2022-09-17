package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.ProductsBySizeEntryHelperFields;
import com.qcadoo.mes.masterOrders.constants.ProductsBySizeHelperFields;
import com.qcadoo.model.api.DataDefinition;
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

    public void changeProductFamily(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductsBySizeHelperFields.PRODUCT);

        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            clearProductsList(view);
            prepareProductsList(view, product);
        } else {
            clearProductsList(view);
        }
    }

    private void clearProductsList(final ViewDefinitionState view) {
        FormComponent productsBySizeHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity productBySizeHelper = getProductsBySizeHelperDD().get(productsBySizeHelperForm.getEntityId());

        for (Entity productsBySizeEntryHelper : productBySizeHelper.getHasManyField(ProductsBySizeHelperFields.PRODUCTS_BY_SIZE_ENTRY_HELPERS)) {
            productsBySizeEntryHelper.getDataDefinition().delete(productsBySizeEntryHelper.getId());
        }
    }


    private void prepareProductsList(final ViewDefinitionState view, final Entity product) {
        FormComponent productsBySizeHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        List<Entity> children = product.getHasManyField(ProductFields.CHILDREN);

        for (Entity child : children) {
            if (Objects.nonNull(child.getBelongsToField(ProductFields.SIZE))) {
                Entity productsBySizeEntryHelper = getProductsBySizeEntryHelperDD().create();

                productsBySizeEntryHelper.setField(ProductsBySizeEntryHelperFields.PRODUCTS_BY_SIZE_HELPER, productsBySizeHelperForm.getEntityId());
                productsBySizeEntryHelper.setField(ProductsBySizeEntryHelperFields.PRODUCT, child.getId());

                productsBySizeEntryHelper.getDataDefinition().save(productsBySizeEntryHelper);
            }
        }
    }

    public void addPositionsToOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }

    private DataDefinition getProductsBySizeHelperDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_HELPER);
    }

    private DataDefinition getProductsBySizeEntryHelperDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_ENTRY_HELPER);
    }

}
