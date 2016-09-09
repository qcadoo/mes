package com.qcadoo.mes.materialFlowResources.exceptions;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;

public class DocumentCreationException extends EntityRuntimeException {

    public DocumentCreationException(Entity entity) {
        super(entity);
    }
}
