package com.qcadoo.mes.orders.states;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.beanutils.MethodUtils;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.util.FieldUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.listeners.OrderDetailsListeners;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;

@Service
public class OperationalTaskOrderStateService {

    private static final Logger LOG = LoggerFactory.getLogger(OperationalTaskOrderStateService.class);

    public static final String FOR_EACH = "03forEach";

    public static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderDetailsListeners orderDetailsListeners;

    @Autowired
    private OperationalTaskStateChangeDescriber operationalTaskStateChangeDescriber;

    @Autowired
    private ShiftsService shiftsService;

    public void startOperationalTask(StateChangeContext stateChangeContext) {
        try {
            Entity order = stateChangeContext.getOwner();
            Session currentSession = getCurrentSession();
            String updateOTHQL = "UPDATE com.qcadoo.model.beans.orders.OrdersOperationalTask SET state = '02started' "
                    + "WHERE order_id = :orderId";
            Query updateOTQuery = currentSession.createQuery(updateOTHQL);
            updateOTQuery.setLong("orderId", order.getId());
            updateOTQuery.executeUpdate();
        } catch (Exception exc) {
            stateChangeContext.addMessage("orders.operationalTask.error.startOperationalTask", StateMessageType.FAILURE);
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            LOG.error("Error when start operational task.", exc);
        }
    }

    private Session getCurrentSession() {
        DataDefinition dataDefinition = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_OPERATIONAL_TASK);
        Object dataAccessService = FieldUtils.getProtectedFieldValue("dataAccessService", dataDefinition);
        Object hibernateService = FieldUtils.getProtectedFieldValue("hibernateService", dataAccessService);

        try {
            return (Session) MethodUtils.invokeExactMethod(hibernateService, "getCurrentSession", new Object[0]);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void rejectOperationalTasksForSchedule(Entity schedule) {
        String userLogin = securityService.getCurrentUserName();
        Entity shift = shiftsService.getShiftFromDateWithTime(new Date());
        List<Entity> positions = schedule.getHasManyField(ScheduleFields.POSITIONS);
        try {
            for (Entity pos : positions) {
                Entity operationalTask = dataDefinitionService
                        .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                        .add(SearchRestrictions.belongsTo(OperationalTaskFields.SCHEDULE_POSITION, pos)).setMaxResults(1)
                        .uniqueResult();
                if (Objects.nonNull(operationalTask)) {
                    changeOperationalTaskState(userLogin, operationalTask,
                            operationalTask.getStringField(OperationalTaskFields.STATE),
                            OperationalTaskStateStringValues.REJECTED, shift);
                }
            }
        } catch (Exception exc) {
            schedule.addGlobalError("orders.operationalTask.error.rejectOperationalTask");
            LOG.error("Error when reject operational task.", exc);

        }
    }

    private void changeOperationalTaskState(String userLogin, Entity ot, String sourceState, String targetState, Entity shift) {
        Entity context = stateExecutorService.buildStateChangeEntity(operationalTaskStateChangeDescriber, ot, userLogin,
                sourceState, targetState, shift);
        context.setField("status", StateChangeStatus.SUCCESSFUL.getStringValue());
        context.getDataDefinition().fastSave(context);
        ot.setField(OperationalTaskFields.STATE, targetState);
        ot.getDataDefinition().fastSave(ot);
    }

    public void rejectOperationalTask(StateChangeContext stateChangeContext) {
        try {
            Entity order = stateChangeContext.getOwner();
            Session currentSession = getCurrentSession();
            String updateOTHQL = "UPDATE com.qcadoo.model.beans.orders.OrdersOperationalTask SET state = '04rejected' "
                    + "WHERE order_id = :orderId";
            Query updateOTQuery = currentSession.createQuery(updateOTHQL);
            updateOTQuery.setLong("orderId", order.getId());
            updateOTQuery.executeUpdate();
        } catch (Exception exc) {
            stateChangeContext.addMessage("orders.operationalTask.error.rejectOperationalTask", StateMessageType.FAILURE);
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            LOG.error("Error when reject operational task.", exc);

        }
    }

    public void finishOperationalTask(StateChangeContext stateChangeContext) {
        try {
            Entity order = stateChangeContext.getOwner();
            Session currentSession = getCurrentSession();
            String updateOTHQL = "UPDATE com.qcadoo.model.beans.orders.OrdersOperationalTask SET state = '03finished' "
                    + "WHERE order_id = :orderId AND state = '02started'";
            Query updateOTQuery = currentSession.createQuery(updateOTHQL);
            updateOTQuery.setLong("orderId", order.getId());
            updateOTQuery.executeUpdate();
        } catch (Exception exc) {
            stateChangeContext.addMessage("orders.operationalTask.error.finishOperationalTask", StateMessageType.FAILURE);
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            LOG.error("Error when finish operational task.", exc);

        }
    }

    public void generateOperationalTasks(StateChangeContext stateChangeContext) {
        if (parameterService.getParameter().getBooleanField("automaticallyGenerateTasksForOrder")) {
            Entity order = stateChangeContext.getOwner();

            if (FOR_EACH.equals(order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))
                    && order.getHasManyField(OrderFields.OPERATIONAL_TASKS).isEmpty()) {
                orderDetailsListeners.createOperationalTasksForOrder(order, true);
            }

        }

    }
}
