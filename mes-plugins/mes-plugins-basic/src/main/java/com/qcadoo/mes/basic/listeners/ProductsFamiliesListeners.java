package com.qcadoo.mes.basic.listeners;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.tree.ProductsFamiliesTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductsFamiliesListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductsFamiliesTreeService productsFamiliesTreeService;

    class FormValidationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        FormValidationException(final String msg) {
            super(msg);
        }
    }

    public final void generateHierarchyTree(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        try {
            Entity formEntity = generateFormEntity(view, state);

            FormComponent form = getForm(view);
            form.setEntity(formEntity);
            view.getComponentByReference("genealogyTree").setEnabled(true);
        } catch (FormValidationException e) {
            state.addMessage(e.getMessage(), MessageType.FAILURE);
        }
    }

    final Entity generateFormEntity(final ViewDefinitionState view, final ComponentState state) {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        FormComponent form = getForm(view);
        Entity formEntity = form.getEntity();
        GridComponent parentsGrid = (GridComponent) view.getComponentByReference("parents");
        Long productId = parentsGrid.getSelectedEntities().get(0).getId();

        if (productId == null) {
            throw new FormValidationException("advancedGenealogy.genealogyTree.noBatchSelected");
        }

        Entity product = dd.get(productId);

        List<Entity> tree = new ArrayList<Entity>();
        tree.add(product);

        EntityTree entityTree = EntityTreeUtilsService.getDetachedEntityTree(tree);
        formEntity.setField("familyHierarchyTree", entityTree);

        return formEntity;
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference("form");
    }
}
