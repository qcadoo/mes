package com.qcadoo.mes.basic.listeners;

import static com.qcadoo.mes.basic.constants.ProductFields.PRODUCT_FAMILY_CHILDREN_TREE;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.hooks.ProductsFamiliesHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductsFamiliesListeners {

    @Autowired
    private ProductsFamiliesHooks productsFamiliesHooks;

    public final void generateHierarchyTree(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productsFamiliesHooks.generateTreeWhenIdIsSet(view);
    }

    public final void editSelectedProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        TreeComponent tree = (TreeComponent) view.getComponentByReference(PRODUCT_FAMILY_CHILDREN_TREE);
        Long productId = tree.getSelectedEntityId();

        if (productId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", productId);

        String url = "../page/basic/productDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void enabledEditButton(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup edit = (RibbonGroup) window.getRibbon().getGroupByName("edit");
        RibbonActionItem editSelectedProductFromTree = edit.getItemByName("editSelectedProduct");
        editSelectedProductFromTree.setEnabled(true);
        editSelectedProductFromTree.requestUpdate(true);
    }

}
