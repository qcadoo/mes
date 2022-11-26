package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.BasicConstants;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TechnologicalProcessRateDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (Objects.nonNull(formComponent.getEntityId())) {
            FieldComponent currentRateFieldComponent = (FieldComponent) view.getComponentByReference("currentRate");
            BigDecimal currentRate = findCurrentRate(formComponent.getEntityId());
            currentRateFieldComponent.setFieldValue(numberService.formatWithMinimumFractionDigits(currentRate, 0));
        }
    }

    private BigDecimal findCurrentRate(Long entityId) {
        Entity technologicalProcessRate = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, "technologicalProcessRate")
                .get(entityId);

        SearchCriteriaBuilder scb = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, "technologicalProcessRateItem")
                .find().addOrder(SearchOrders.desc("dateFrom"))
                .add(SearchRestrictions.belongsTo("technologicalProcessRate", technologicalProcessRate))
                .add(SearchRestrictions.le("dateFrom", new Date()));
        Entity technologicalProcessRateItem = scb.setMaxResults(1).uniqueResult();
        if (Objects.isNull(technologicalProcessRateItem)) {
            return null;
        } else {
            return technologicalProcessRateItem.getDecimalField("actualRate");
        }

    }
}
