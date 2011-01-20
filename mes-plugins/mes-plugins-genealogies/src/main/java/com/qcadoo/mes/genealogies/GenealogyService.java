package com.qcadoo.mes.genealogies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.awesomeDynamicList.AwesomeDynamicListState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;

@Service
public final class GenealogyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void newBatch(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String[] args) {
        ((GridComponentState) viewDefinitionState.getComponentByReference("grid")).setSelectedEntityId(null);
    }

    public void showGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = ((FormComponentState) triggerState).getEntityId();

        if (orderId != null) {
            String url = "../page/genealogies/orderGenealogies.html?context={\"order.id\":\"" + orderId + "\",\"form.order\":\""
                    + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false);
        }
    }

    public void fillProductInComponents(final ViewDefinitionState state, final Locale locale) {
        FormComponentState orderForm = (FormComponentState) state.getComponentByReference("order");
        FormComponentState genealogyForm = (FormComponentState) state.getComponentByReference("form");
        AwesomeDynamicListState list = (AwesomeDynamicListState) state.getComponentByReference("productInComponentsList");

        Entity genealogy = null;
        List<Entity> existingProductInComponents = Collections.emptyList();

        if (genealogyForm.getEntityId() != null) {
            genealogy = dataDefinitionService.get("genealogies", "genealogy").get(genealogyForm.getEntityId());
            existingProductInComponents = genealogy.getHasManyField("productInComponents");
        }

        Entity order = dataDefinitionService.get("products", "order").get(orderForm.getEntityId());
        Entity technology = order.getBelongsToField("technology");

        List<Entity> targetProductInComponents = new ArrayList<Entity>();

        for (Entity operationComponent : technology.getHasManyField("operationComponents")) {
            for (Entity operationProductInComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                if ((Boolean) operationProductInComponent.getField("batchRequired")) {
                    targetProductInComponents.add(createGenealogyProductInComponent(genealogy, operationProductInComponent,
                            existingProductInComponents));
                }
            }
        }

        list.setFieldValue(targetProductInComponents);
    }

    private Entity createGenealogyProductInComponent(final Entity genealogy, final Entity operationProductInComponent,
            final List<Entity> existingProductInComponents) {
        for (Entity e : existingProductInComponents) {
            if (e.getBelongsToField("productInComponent").getId().equals(operationProductInComponent.getId())) {
                return e;
            }
        }
        Entity genealogyProductInComponent = new DefaultEntity("genealogies", "genealogyProductInComponent");
        genealogyProductInComponent.setField("genealogy", genealogy);
        genealogyProductInComponent.setField("productInComponent", operationProductInComponent);
        genealogyProductInComponent.setField("batch", new ArrayList<Entity>());
        return genealogyProductInComponent;
    }

}
