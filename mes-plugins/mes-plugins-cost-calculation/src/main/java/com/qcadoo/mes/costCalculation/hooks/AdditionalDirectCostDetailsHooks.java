package com.qcadoo.mes.costCalculation.hooks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.AdditionalDirectCostFields;
import com.qcadoo.mes.costCalculation.constants.AdditionalDirectCostItemFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class AdditionalDirectCostDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (Objects.nonNull(formComponent.getEntityId())) {
            FieldComponent currentCostFieldComponent = (FieldComponent) view.getComponentByReference(AdditionalDirectCostFields.CURRENT_COST);
            BigDecimal currentCost = findCurrentCost(formComponent.getEntityId());
            currentCostFieldComponent.setFieldValue(numberService.formatWithMinimumFractionDigits(currentCost, 0));
        }
    }

    private BigDecimal findCurrentCost(Long entityId) {
        Entity additionalDirectCost = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER, CostCalculationConstants.MODEL_ADDITIONAL_DIRECT_COST)
                .get(entityId);

        SearchCriteriaBuilder scb = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER, CostCalculationConstants.MODEL_ADDITIONAL_DIRECT_COST_ITEM)
                .find().addOrder(SearchOrders.desc(AdditionalDirectCostItemFields.DATE_FROM))
                .add(SearchRestrictions.belongsTo(AdditionalDirectCostItemFields.ADDITIONAL_DIRECT_COST, additionalDirectCost))
                .add(SearchRestrictions.le(AdditionalDirectCostItemFields.DATE_FROM, new Date()));
        Entity additionalDirectCostItem = scb.setMaxResults(1).uniqueResult();
        if (Objects.isNull(additionalDirectCostItem)) {
            return null;
        } else {
            return additionalDirectCostItem.getDecimalField(AdditionalDirectCostItemFields.ACTUAL_COST);
        }

    }
}
