package com.qcadoo.mes.masterOrders.hooks;

import java.util.Collections;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.SalesPlanFields;
import com.qcadoo.mes.masterOrders.states.SalesPlanServiceMarker;
import com.qcadoo.mes.masterOrders.states.constants.SalesPlanStateStringValues;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class SalesPlanHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private StateExecutorService stateExecutorService;

    public void onCreate(final DataDefinition salesPlanDD, final Entity salesPlan) {
        setInitialState(salesPlan);
    }

    public void onCopy(final DataDefinition salesPlanDD, final Entity salesPlan) {
        setInitialState(salesPlan);
    }

    private void setInitialState(final Entity salesPlan) {
        stateExecutorService.buildInitial(SalesPlanServiceMarker.class, salesPlan, SalesPlanStateStringValues.DRAFT);
    }

    public void onSave(final DataDefinition salesPlanDD, final Entity salesPlan) {
        setSalesPlanNumber(salesPlan);
    }

    private void setSalesPlanNumber(final Entity salesPlan) {
        if (checkIfShouldInsertNumber(salesPlan)) {
            String number = jdbcTemplate.queryForObject("select generate_sales_plan_number()", Collections.emptyMap(),
                    String.class);
            salesPlan.setField(SalesPlanFields.NUMBER, number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity salesPlan) {
        if (!Objects.isNull(salesPlan.getId())) {
            return false;
        }
        return !StringUtils.isNotBlank(salesPlan.getStringField(SalesPlanFields.NUMBER));
    }

    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        if (!entity.getHasManyField(SalesPlanFields.ORDERS).isEmpty()
                || !entity.getHasManyField(SalesPlanFields.DELIVERIES).isEmpty()
                || !entity.getHasManyField(SalesPlanFields.MASTER_ORDERS).isEmpty()) {
            entity.addGlobalError("qcadooView.errorPage.error.dataIntegrityViolationException.objectInUse.explanation");
            return false;
        }
        return true;
    }
}
