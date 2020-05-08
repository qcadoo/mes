package com.qcadoo.mes.ordersForSubproductsGeneration.hooks;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.qcadoo.mes.ordersForSubproductsGeneration.OrdersForSubproductsGenerationService;
import com.qcadoo.mes.ordersForSubproductsGeneration.criteriaModifiers.OrdersForSPCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrdersForSubproductsHooks {

    private static final String L_ORDERS = "orders";

    private static final String L_GENERATEAD_ORDERS = "generatedOrders";

    

    @Autowired
    private OrdersForSubproductsGenerationService ordersForSubproductsGenerationService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        setCriteriaModifierParameters(view);
        toggleGenerateOrderButton(view);
    }

    private void toggleGenerateOrderButton(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long id = form.getEntityId();
        boolean isEnabled = false;
        if (Objects.nonNull(id)) {
            Entity entity = form.getEntity().getDataDefinition().get(id);
            isEnabled = !hasGeneratedOrders(entity);
        }

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup group = window.getRibbon().getGroupByName("orders");
        RibbonActionItem generateOrders = group.getItemByName("generateOrders");
        generateOrders.setMessage("ordersForSubproductsGeneration.ordersForSubproducts.generate.msg");
        updateButtonState(generateOrders, isEnabled);
    }

    private List<Long> getOrdersIds(Entity entity) {
        List<Entity> orders = Lists.newArrayList();

        if (Objects.nonNull(entity.getBelongsToField("ordersGroup"))) {
            orders = entity.getBelongsToField("ordersGroup").getHasManyField("orders");
        } else if (Objects.nonNull(entity.getBelongsToField("order"))) {
            orders.add(entity.getBelongsToField("order"));
        }

        return orders.stream().map(o -> o.getId()).collect(Collectors.toList());
    }

    private boolean hasGeneratedOrders(final Entity entity) {
        return ordersForSubproductsGenerationService.hasSubOrders(getOrdersIds(entity));
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent gridOrders = (GridComponent) view.getComponentByReference(L_ORDERS);
        GridComponent gridGeneratedOrders = (GridComponent) view.getComponentByReference(L_GENERATEAD_ORDERS);
        FilterValueHolder gridGeneratedOrdersHolder = gridGeneratedOrders.getFilterValue();
        FilterValueHolder gridOrdersHolder = gridOrders.getFilterValue();
        List<Long> ids = Lists.newArrayList();

        Entity entity = form.getEntity();
        entity = entity.getDataDefinition().get(entity.getId());
        if (Objects.nonNull(entity.getBelongsToField("ordersGroup"))) {
            ids = entity.getBelongsToField("ordersGroup").getHasManyField("orders").stream().map(e -> e.getId())
                    .collect(Collectors.toList());
        } else if (Objects.nonNull(entity.getBelongsToField("order"))) {
            ids.add(entity.getBelongsToField("order").getId());
        }
        String list = Joiner.on(",").join(ids);
        gridGeneratedOrdersHolder.put(OrdersForSPCriteriaModifiers.ORDERS_PARAMETER, list);

        gridGeneratedOrders.setFilterValue(gridGeneratedOrdersHolder);

        gridOrdersHolder.put(OrdersForSPCriteriaModifiers.ORDERS_PARAMETER, list);

        gridOrders.setFilterValue(gridOrdersHolder);

    }

}