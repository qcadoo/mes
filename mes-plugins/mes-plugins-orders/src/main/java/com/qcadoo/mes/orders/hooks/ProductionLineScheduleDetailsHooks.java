package com.qcadoo.mes.orders.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.criteriaModifiers.ProductionLineScheduleOrderCriteriaModifiers;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionLineScheduleDetailsHooks {

    private static final String L_ORDERS_LOOKUP = "ordersLookup";

    public void onBeforeRender(final ViewDefinitionState view) {
        setOrderLookupCriteriaModifier(view);
    }

    private void setOrderLookupCriteriaModifier(final ViewDefinitionState view) {
        FormComponent scheduleForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long scheduleId = scheduleForm.getEntityId();
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(L_ORDERS_LOOKUP);

        FilterValueHolder valueHolder = orderLookup.getFilterValue();
        valueHolder.put(ProductionLineScheduleOrderCriteriaModifiers.SCHEDULE_PARAMETER, scheduleId);
        orderLookup.setFilterValue(valueHolder);
    }

}
