package com.qcadoo.mes.newstates;

import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public abstract class BasicStateService implements StateService {

    @Override
    public Entity onValidate(Entity entity, String sourceState, String targetState) {
        return entity;
    }

    @Override
    public Entity onBeforeSave(Entity entity, String sourceState, String targetState) {
        return entity;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState) {
        return entity;
    }

}
