package com.qcadoo.mes.orders.states.aop.listener;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

import java.util.List;

@Aspect
@Configurable
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class ClearBarCodesOnOrderChangeStateAspect extends AbstractStateListenerAspect {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private BarcodeOperationComponentService barcodeOperationComponentService;

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
        // empty by design
    }

    @RunInPhase(OrderStateChangePhase.LAST)
    @RunForStateTransition(targetState = OrderStateStringValues.ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void afterAccepting(final StateChangeContext stateChangeContext, final int phase) {
        final Entity order = stateChangeContext.getOwner();
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology != null) {
            List<Entity> tocs = technology
                    .getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

            for (Entity toc : tocs) {
                barcodeOperationComponentService.createBarcodeOperationComponent(order, toc);
            }
        }
    }

    @RunInPhase(OrderStateChangePhase.LAST)
    @RunForStateTransition(targetState = OrderStateStringValues.COMPLETED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void afterComplete(final StateChangeContext stateChangeContext, final int phase) {
        clearAssociatedBarcodeOperationComponents(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.LAST)
    @RunForStateTransition(targetState = OrderStateStringValues.ABANDONED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void afterAbandoning(final StateChangeContext stateChangeContext, final int phase) {
        clearAssociatedBarcodeOperationComponents(stateChangeContext);
    }

    private void clearAssociatedBarcodeOperationComponents(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        if (order != null && order.getId() != null) {
            String query = "DELETE FROM technologies_barcodeoperationcomponent WHERE order_id = :id";
            jdbcTemplate.update(query, new MapSqlParameterSource("id", order.getId()));
        }
    }

}