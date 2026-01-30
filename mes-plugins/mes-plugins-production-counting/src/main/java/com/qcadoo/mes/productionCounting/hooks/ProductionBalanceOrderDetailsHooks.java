package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionBalanceOrderDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        setCriteriaModifierParameters(view);
    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view) throws JSONException {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Long productionBalanceId = Long.valueOf(view.getJsonContext().get("window.mainTab.form.productionBalance").toString());
        DataDefinition productionBalanceDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_BALANCE);
        Entity productionBalance = productionBalanceDD.get(productionBalanceId);
        List<Entity> orders = productionBalance.getManyToManyField(ProductionBalanceFields.ORDERS);

        FilterValueHolder filterValueHolder = grid.getFilterValue();

        if (orders.isEmpty()) {
            filterValueHolder.remove(ProductionBalanceFields.ORDERS);
        } else {
            filterValueHolder.put(ProductionBalanceFields.ORDERS, orders.stream().map(Entity::getId).collect(Collectors.toList()));
        }
        grid.setFilterValue(filterValueHolder);
    }

}
