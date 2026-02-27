package com.qcadoo.mes.productionCounting.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.GlobalMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.grid.GridComponentFilterSQLUtils;
import com.qcadoo.view.api.components.grid.GridComponentMultiSearchFilter;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductionBalanceOrderDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void createOrders(final ViewDefinitionState view, final ComponentState state, final String args[]) throws JSONException {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Long productionBalanceId = Long.valueOf(view.getJsonContext().get("window.mainTab.form.productionBalance").toString());
        DataDefinition productionBalanceDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_BALANCE);
        Entity productionBalance = productionBalanceDD.get(productionBalanceId);
        DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
        List<Entity> orders = grid.getSelectedEntities().stream().map(e -> orderDD.get(e.getId())).collect(Collectors.toList());
        orders.addAll(productionBalance.getManyToManyField(ProductionBalanceFields.ORDERS));
        productionBalance.setField(ProductionBalanceFields.ORDERS, orders);
        productionBalanceDD.save(productionBalance);
    }

    public final void addAllOrdersWithCriteria(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        Long productionBalanceId = Long.valueOf(view.getJsonContext().get("window.mainTab.form.productionBalance").toString());
        DataDefinition productionBalanceDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_BALANCE);
        Entity productionBalance = productionBalanceDD.get(productionBalanceId);
        List<Entity> orders = Lists.newArrayList(productionBalance.getHasManyField(ProductionBalanceFields.ORDERS));
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        String query = "SELECT id FROM orders_orderplanninglistdto ";

        Map<String, String> filter = grid.getFilters();
        GridComponentMultiSearchFilter multiSearchFilter = grid.getMultiSearchFilter();
        String filterQ;
        try {
            filterQ = GridComponentFilterSQLUtils.addFilters(filter, grid.getColumns(),
                    "orders_orderplanninglistdto",
                    dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                            OrdersConstants.MODEL_ORDER_PLANNING_LIST_DTO));
            filterQ += " AND ";
            filterQ += GridComponentFilterSQLUtils.addMultiSearchFilter(multiSearchFilter, grid.getColumns(),
                    "orders_orderplanninglistdto",
                    dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                            OrdersConstants.MODEL_ORDER_PLANNING_LIST_DTO));
        } catch (Exception e) {
            filterQ = "";
        }

        query = query + " WHERE state in ('03inProgress','04completed','06interrupted','07abandoned') AND active = true ";
        if (StringUtils.isNoneBlank(filterQ)) {
            query = query + " AND " + filterQ;
        }

        List<Long> ids = jdbcTemplate.queryForList(query, Collections.emptyMap(), Long.class);
        if (ids.isEmpty()) {
            view.addMessage(new GlobalMessage("productionCounting.productionBalance.error.noOrders"));
            return;
        }
        orders.addAll(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.in("id", ids)).list().getEntities());
        productionBalance.setField(ProductionBalanceFields.ORDERS, orders);
        productionBalanceDD.save(productionBalance);
    }

}
