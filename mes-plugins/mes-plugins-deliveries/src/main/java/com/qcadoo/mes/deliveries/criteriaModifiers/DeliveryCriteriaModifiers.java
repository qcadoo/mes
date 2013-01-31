package com.qcadoo.mes.deliveries.criteriaModifiers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveryCriteriaModifiers {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showActiveSupplyItems(final SearchCriteriaBuilder scb) {
        final SearchQueryBuilder sqb = getOrderedProductDataDefinition()
                .find("select orderedProduct from #deliveries_orderedProduct as orderedProduct INNER JOIN orderedProduct.delivery as delivery WHERE delivery.active = 'true'");
        List<Entity> orderedProductsList = sqb.list().getEntities();
        List<Long> orderedProductsListLong = new ArrayList<Long>();
        for (Entity entity : orderedProductsList) {
            orderedProductsListLong.add(entity.getId());
        }
        if (orderedProductsListLong.isEmpty()) {
            final BigDecimal bg = new BigDecimal(-1);
            scb.add(SearchRestrictions.eq("orderedQuantity", bg));
        } else {
            scb.add(SearchRestrictions.in("id", orderedProductsListLong));
        }
    }

    private DataDefinition getOrderedProductDataDefinition() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT);
    }
}
