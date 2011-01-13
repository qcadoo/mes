package com.qcadoo.mes.internal;

import com.qcadoo.mes.api.Entity;

@SuppressWarnings("serial")
public class CopyException extends RuntimeException {

    private final Entity entity;

    public CopyException(final Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

}
