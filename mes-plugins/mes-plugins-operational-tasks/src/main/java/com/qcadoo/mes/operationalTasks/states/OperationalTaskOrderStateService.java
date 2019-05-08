package com.qcadoo.mes.operationalTasks.states;

import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationalTaskOrderStateService {

    private static final Logger LOG = LoggerFactory.getLogger(OperationalTaskOrderStateService.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private SecurityService securityService;

    public void startOperationalTask(StateChangeContext stateChangeContext) {
        try {
            Entity order = stateChangeContext.getOwner();
            List<Entity> tasksForOrder = dataDefinitionService
                    .get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK).find()
                    .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

            String userLogin = securityService.getCurrentUserName();
            for (Entity ot : tasksForOrder) {
                stateExecutorService.changeState(OperationalTasksServiceMarker.class, ot, userLogin,
                        OperationalTaskStateStringValues.STARTED);
            }
        } catch (Exception exc) {
            stateChangeContext.addMessage("operationalTasks.operationalTask.error.startOperationalTask", StateMessageType.FAILURE);
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            LOG.error("Error when start operational task.", exc);
        }
    }
}
