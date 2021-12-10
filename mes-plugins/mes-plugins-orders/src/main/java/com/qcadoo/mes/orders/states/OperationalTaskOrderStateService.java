package com.qcadoo.mes.orders.states;

import com.qcadoo.mes.basic.ParameterService;
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
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void startOperationalTask(StateChangeContext stateChangeContext) {
        try {
            Entity order = stateChangeContext.getOwner();
            List<Entity> tasksForOrder = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                    .add(SearchRestrictions.belongsTo(OperationalTaskFields.ORDER, order)).list().getEntities();

            String userLogin = securityService.getCurrentUserName();
            for (Entity ot : tasksForOrder) {
                changeOperationalTaskState(userLogin, ot, ot.getStringField(OperationalTaskFields.STATE),
                        OperationalTaskStateStringValues.STARTED);
            }
        } catch (Exception exc) {
            stateChangeContext.addMessage("orders.operationalTask.error.startOperationalTask", StateMessageType.FAILURE);
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            LOG.error("Error when start operational task.", exc);
        }
    }

    public void rejectOperationalTasksForSchedule(Entity schedule) {
        String userLogin = securityService.getCurrentUserName();
        List<Entity> positions = schedule.getHasManyField(ScheduleFields.POSITIONS);
        try {
            for (Entity pos : positions) {
                Entity operationalTask = dataDefinitionService
                        .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                        .add(SearchRestrictions.belongsTo(OperationalTaskFields.SCHEDULE_POSITION, pos)).setMaxResults(1)
                        .uniqueResult();
                if (Objects.nonNull(operationalTask)) {
                    changeOperationalTaskState(userLogin, operationalTask, operationalTask.getStringField(OperationalTaskFields.STATE),
                            OperationalTaskStateStringValues.REJECTED);
                }
            }
        } catch (Exception exc) {
            schedule.addGlobalError("orders.operationalTask.error.rejectOperationalTask");
            LOG.error("Error when reject operational task.", exc);

        }
    }

    public void rejectOperationalTask(StateChangeContext stateChangeContext) {
        try {
            Entity order = stateChangeContext.getOwner();
            List<Entity> tasksForOrder = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                    .add(SearchRestrictions.belongsTo(OperationalTaskFields.ORDER, order)).list().getEntities();

            String userLogin = securityService.getCurrentUserName();
            for (Entity ot : tasksForOrder) {
                changeOperationalTaskState(userLogin, ot, ot.getStringField(OperationalTaskFields.STATE),
                        OperationalTaskStateStringValues.REJECTED);
            }
        } catch (Exception exc) {
            stateChangeContext.addMessage("orders.operationalTask.error.rejectOperationalTask", StateMessageType.FAILURE);
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            LOG.error("Error when reject operational task.", exc);

        }
    }

    private void changeOperationalTaskState(String userLogin, Entity ot, String sourceState, String targetState) {
        Entity context = stateExecutorService.buildStateChangeEntity(operationalTaskStateChangeDescriber, ot, userLogin,
                sourceState, targetState);
        context.setField("status", StateChangeStatus.SUCCESSFUL.getStringValue());
        context.getDataDefinition().fastSave(context);
        ot.setField(OperationalTaskFields.STATE, targetState);
        ot.getDataDefinition().fastSave(ot);
    }

    public void finishOperationalTask(StateChangeContext stateChangeContext) {
        try {
            Entity order = stateChangeContext.getOwner();
            List<Entity> tasksForOrder = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                    .add(SearchRestrictions.belongsTo(OperationalTaskFields.ORDER, order))
                    .add(SearchRestrictions.eq(OperationalTaskFields.STATE, OperationalTaskStateStringValues.STARTED)).list()
                    .getEntities();

            String userLogin = securityService.getCurrentUserName();
            for (Entity ot : tasksForOrder) {
                changeOperationalTaskState(userLogin, ot, ot.getStringField(OperationalTaskFields.STATE),
                        OperationalTaskStateStringValues.FINISHED);
            }
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
