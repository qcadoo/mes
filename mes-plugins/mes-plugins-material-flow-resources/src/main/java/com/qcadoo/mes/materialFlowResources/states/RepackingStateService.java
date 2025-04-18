package com.qcadoo.mes.materialFlowResources.states;

import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.mes.materialFlowResources.states.constants.RepackingStateChangeDescriber;
import com.qcadoo.mes.materialFlowResources.states.constants.RepackingStateStringValues;
import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepackingStateService extends BasicStateService implements RepackingServiceMarker {

    @Autowired
    private RepackingStateChangeDescriber repackingStateChangeDescriber;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return repackingStateChangeDescriber;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                              StateChangeEntityDescriber describer) {
        switch (targetState) {
            case RepackingStateStringValues.ACCEPTED:
                resourceManagementService.repackageResources(entity);
                break;
        }

        return entity;
    }

}
