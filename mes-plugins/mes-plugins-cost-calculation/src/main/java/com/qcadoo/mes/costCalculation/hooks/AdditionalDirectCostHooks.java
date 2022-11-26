package com.qcadoo.mes.costCalculation.hooks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.AdditionalDirectCostFields;
import com.qcadoo.mes.costCalculation.constants.AdditionalDirectCostItemFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AdditionalDirectCostHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onView(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(AdditionalDirectCostFields.CURRENT_COST, findCurrentCost(entity));
    }

    private BigDecimal findCurrentCost(Entity entity) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(CostCalculationConstants.PLUGIN_IDENTIFIER, CostCalculationConstants.MODEL_ADDITIONAL_DIRECT_COST_ITEM)
                .find().addOrder(SearchOrders.desc(AdditionalDirectCostItemFields.DATE_FROM))
                .add(SearchRestrictions.belongsTo(AdditionalDirectCostItemFields.ADDITIONAL_DIRECT_COST, entity))
                .add(SearchRestrictions.le(AdditionalDirectCostItemFields.DATE_FROM, new Date()));
        Entity additionalDirectCostItem = scb.setMaxResults(1).uniqueResult();
        if(Objects.isNull(additionalDirectCostItem)) {
            return null;
        } else {
            return additionalDirectCostItem.getDecimalField(AdditionalDirectCostItemFields.ACTUAL_COST);
        }

    }

}
