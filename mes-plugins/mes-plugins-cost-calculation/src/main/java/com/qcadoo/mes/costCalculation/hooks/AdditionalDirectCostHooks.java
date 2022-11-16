package com.qcadoo.mes.costCalculation.hooks;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        setNumber(entity);
    }

    public void onView(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(AdditionalDirectCostFields.CURRENT_COST, findCurrentCost(entity));
    }

    private void setNumber(final Entity entity) {
        if (checkIfShouldInsertNumber(entity)) {
            String number = jdbcTemplate.queryForObject("select generate_additional_direct_cost_number()", Collections.emptyMap(),
                    String.class);
            entity.setField(AdditionalDirectCostFields.NUMBER, number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity entity) {
        if (!Objects.isNull(entity.getId())) {
            return false;
        }
        return !StringUtils.isNotBlank(entity.getStringField(AdditionalDirectCostFields.NUMBER));
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
