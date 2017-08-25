package com.qcadoo.mes.materialFlowResources.exceptions;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;

public final class InvalidResourceException extends EntityRuntimeException {

    public InvalidResourceException(Entity entity) {
        super(entity);
    }
}
