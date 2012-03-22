package com.qcadoo.mes.technologies.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.TechnologyStateBeforeChangeNotifierService.BeforeStateChangeListener;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;

@Component
public class TechnologyBeforeStateChangeListener implements BeforeStateChangeListener {

    @Autowired
    private TechnologyService technologyService;

    @Override
    public boolean canChange(ComponentState gridOrForm, Entity technology, TechnologyState newState) {
        if ((TechnologyState.OUTDATED.equals(newState) && technologyService.isTechnologyUsedInActiveOrder(technology))
                || (TechnologyState.DECLINED.equals(newState) && technologyService.isTechnologyUsedInActiveOrder(technology))) {
            gridOrForm.addMessage("technologies.technology.state.error.orderInProgress", MessageType.FAILURE);

            return false;
        }
        return true;
    }
}
