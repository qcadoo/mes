package com.qcadoo.mes.newstates;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;

public interface StateService {

    public Entity onValidate(Entity entity, String sourceState, String targetState);

    public Entity onBeforeSave(Entity entity, String sourceState, String targetState);

    public Entity onAfterSave(Entity entity, String sourceState, String targetState);

    public StateChangeEntityDescriber getChangeEntityDescriber();

}
