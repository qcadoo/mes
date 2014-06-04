package com.qcadoo.mes.technologies.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationProductComponentHolder {

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    private static final String L_PRODUCT = "product";

    private final Long productId;

    private final Long technologyOperationComponentId;

    private final DataDefinition productDD;

    private final DataDefinition technologyOperationComponentDD;

    private final OperationProductComponentEntityType entityType;

    public OperationProductComponentHolder(final Entity operationProductComponent) {
        Entity product = operationProductComponent.getBelongsToField(L_PRODUCT);
        Entity technologyOperationComponent = operationProductComponent.getBelongsToField(L_OPERATION_COMPONENT);

        OperationProductComponentEntityType entityType = OperationProductComponentEntityType
                .parseString(operationProductComponent.getDataDefinition().getName());

        this.productId = product.getId();
        this.technologyOperationComponentId = technologyOperationComponent.getId();
        this.productDD = product.getDataDefinition();
        this.technologyOperationComponentDD = technologyOperationComponent.getDataDefinition();
        this.entityType = entityType;
    }

    public OperationProductComponentHolder(final Entity product, final Entity technologyOperationComponent,
            final OperationProductComponentEntityType entityType) {

        Long productId = product.getId();
        Long technologyOperationComponentId = (technologyOperationComponent == null) ? null : technologyOperationComponent
                .getId();
        DataDefinition productDD = product.getDataDefinition();
        DataDefinition technologyOperationComponentDD = (technologyOperationComponent == null) ? null
                : technologyOperationComponent.getDataDefinition();

        this.productId = productId;
        this.technologyOperationComponentId = technologyOperationComponentId;
        this.productDD = productDD;
        this.technologyOperationComponentDD = technologyOperationComponentDD;
        this.entityType = entityType;
    }

    public OperationProductComponentHolder(final Long productId, final Long technologyOperationComponentId,
            final DataDefinition productDD, final DataDefinition technologyOperationComponentDD,
            final OperationProductComponentEntityType entityType) {
        this.productId = productId;
        this.technologyOperationComponentId = technologyOperationComponentId;
        this.productDD = productDD;
        this.technologyOperationComponentDD = technologyOperationComponentDD;
        this.entityType = entityType;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getTechnologyOperationComponentId() {
        return technologyOperationComponentId;
    }

    public DataDefinition getProductDD() {
        return productDD;
    }

    public DataDefinition getTechnologyOperationComponentDD() {
        return technologyOperationComponentDD;
    }

    public OperationProductComponentEntityType getEntityType() {
        return entityType;
    }

    public Entity getProduct() {
        if ((getProductId() == null) || (getProductDD() == null)) {
            return null;
        } else {
            return getProductDD().get(getProductId());
        }
    }

    public Entity getTechnologyOperationComponent() {
        if ((getTechnologyOperationComponentId() == null) || (getTechnologyOperationComponentDD() == null)) {
            return null;
        } else {
            return getTechnologyOperationComponentDD().get(getTechnologyOperationComponentId());
        }
    }

    public boolean isEntityTypeSame(final String operationProductComponentModelName) {
        return isEntityTypeSame(OperationProductComponentEntityType.parseString(operationProductComponentModelName));
    }

    public boolean isEntityTypeSame(final OperationProductComponentEntityType operationProductComponentEntityType) {
        return operationProductComponentEntityType.equals(getEntityType());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(productId).append(technologyOperationComponentId).append(entityType).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OperationProductComponentHolder)) {
            return false;
        }

        OperationProductComponentHolder other = (OperationProductComponentHolder) obj;

        return new EqualsBuilder().append(productId, other.productId)
                .append(technologyOperationComponentId, other.technologyOperationComponentId)
                .append(entityType, other.entityType).isEquals();
    }

}
