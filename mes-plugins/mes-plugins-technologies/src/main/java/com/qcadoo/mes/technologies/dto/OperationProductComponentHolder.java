package com.qcadoo.mes.technologies.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationProductComponentHolder {

    private final Long entityId;

    private final DataDefinition entityDD;

    private final OperationProductComponentEntityType entityType;

    public OperationProductComponentHolder(final Entity operationProductComponent) {
        this(operationProductComponent.getId(), operationProductComponent.getDataDefinition(),
                OperationProductComponentEntityType.parseString(operationProductComponent.getDataDefinition().getName()));
    }

    public OperationProductComponentHolder(final Long entityId, final DataDefinition entityDD,
            final OperationProductComponentEntityType entityType) {
        this.entityId = entityId;
        this.entityDD = entityDD;
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public DataDefinition getEntityDD() {
        return entityDD;
    }

    public OperationProductComponentEntityType getEntityType() {
        return entityType;
    }

    public Entity getEntity() {
        return getEntityDD().get(getEntityId());
    }

    public boolean isEntityTypeSame(final OperationProductComponentEntityType operationProductComponentEntityType) {
        return operationProductComponentEntityType.equals(getEntityType());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(entityId).append(entityType).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof OperationProductComponentHolder)) {
            return false;
        }

        OperationProductComponentHolder other = (OperationProductComponentHolder) obj;

        return new EqualsBuilder().append(entityId, other.entityId).append(entityType, other.entityType).isEquals();
    }

}
