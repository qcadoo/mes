package com.qcadoo.mes.masterOrders.listeners;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class SalesPlanUseOtherTechnologyListeners {

    public static final String L_GENERATED = "generated";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void update(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);
        String ordersIds = view.getJsonContext().get("window.mainTab.salesPlanProduct.gridLayout.salesPlanProductsIds")
                .toString();
        List<Long> salesPlanProductsIds = Lists.newArrayList(ordersIds.split(",")).stream().map(Long::valueOf)
                .collect(Collectors.toList());
        DataDefinition salesPlanProductDataDefinition = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT);
        List<Entity> salesPlanProducts = salesPlanProductDataDefinition.find()
                .add(SearchRestrictions.in("id", salesPlanProductsIds)).list().getEntities();
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(SalesPlanProductFields.TECHNOLOGY);
        Entity technology = technologyLookup.getEntity();
        if (technology == null) {
            technologyLookup.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);
            view.addMessage("qcadooView.validate.global.error.custom", ComponentState.MessageType.FAILURE);
            return;
        }
        for (Entity salesPlanProduct : salesPlanProducts) {
            salesPlanProduct.setField(SalesPlanProductFields.TECHNOLOGY, technology);
            salesPlanProductDataDefinition.save(salesPlanProduct);
        }
        view.addMessage("masterOrders.salesPlanUseOtherTechnology.update.success", ComponentState.MessageType.SUCCESS);
        generated.setChecked(true);
    }
}
