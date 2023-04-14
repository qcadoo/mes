/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologies.dto;

import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OperationProductComponentHolder {

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    private static final String L_PRODUCT = "product";

    private final Long productId;

    private final Long technologyOperationComponentId;

    private final Long productionCountingQuantityId;

    private final Long operationProductComponentId;

    private final Long technologyInputProductTypeId;

    private final Long attributeId;

    private final DataDefinition productDD;

    private final DataDefinition technologyOperationComponentDD;

    private final DataDefinition operationProductComponentDD;

    private final DataDefinition technologyInputProductTypeDD;

    private final DataDefinition attributeDD;

    private final OperationProductComponentEntityType entityType;

    private final ProductMaterialType productMaterialType;

    private final Boolean waste;

    public OperationProductComponentHolder(final Entity operationProductComponent) {
        Entity product = operationProductComponent.getBelongsToField(L_PRODUCT);
        Entity technologyOperationComponent = operationProductComponent.getBelongsToField(L_OPERATION_COMPONENT);

        OperationProductComponentEntityType entityType = OperationProductComponentEntityType
                .parseString(operationProductComponent.getDataDefinition().getName());

        this.productId = product != null ? product.getId() : null;
        this.technologyOperationComponentId = technologyOperationComponent.getId();
        this.productDD = product != null ? product.getDataDefinition() : null;
        this.technologyOperationComponentDD = technologyOperationComponent.getDataDefinition();
        this.entityType = entityType;
        this.productMaterialType = ProductMaterialType.NONE;
        this.productionCountingQuantityId = null;
        if (entityType.getStringValue().equals(
                OperationProductComponentEntityType.OPERATION_PRODUCT_IN_COMPONENT.getStringValue())) {
            Entity technologyInputProductType = operationProductComponent
                    .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
            if (Objects.nonNull(technologyInputProductType)) {
                this.technologyInputProductTypeId = technologyInputProductType.getId();
                this.technologyInputProductTypeDD = technologyInputProductType.getDataDefinition();
            } else {
                this.technologyInputProductTypeId = null;
                this.technologyInputProductTypeDD = null;
            }
            Entity attribute = operationProductComponent
                    .getBelongsToField(OperationProductInComponentFields.ATTRIBUTE);
            if (Objects.nonNull(attribute)) {
                this.attributeId = attribute.getId();
                this.attributeDD = attribute.getDataDefinition();
            } else {
                this.attributeId = null;
                this.attributeDD = null;
            }
            this.waste = false;
        } else {
            this.technologyInputProductTypeId = null;
            this.technologyInputProductTypeDD = null;
            this.attributeId = null;
            this.attributeDD = null;
            this.waste = operationProductComponent.getBooleanField(OperationProductOutComponentFields.WASTE);

        }
        this.operationProductComponentId = operationProductComponent.getId();
        this.operationProductComponentDD = operationProductComponent.getDataDefinition();
    }

    public OperationProductComponentHolder(final Entity operationProductComponent, final Entity product) {
        Entity technologyOperationComponent = operationProductComponent.getBelongsToField(L_OPERATION_COMPONENT);

        OperationProductComponentEntityType entityType = OperationProductComponentEntityType
                .parseString(operationProductComponent.getDataDefinition().getName());

        this.productId = product.getId();
        this.technologyOperationComponentId = technologyOperationComponent.getId();
        this.productDD = product.getDataDefinition();
        this.technologyOperationComponentDD = technologyOperationComponent.getDataDefinition();
        this.entityType = entityType;
        this.productMaterialType = ProductMaterialType.NONE;
        this.waste = false;

        this.productionCountingQuantityId = null;
        if (entityType.getStringValue().equals(
                OperationProductComponentEntityType.OPERATION_PRODUCT_IN_COMPONENT.getStringValue())) {
            Entity technologyInputProductType = operationProductComponent
                    .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
            if (Objects.nonNull(technologyInputProductType)) {
                this.technologyInputProductTypeId = technologyInputProductType.getId();
                this.technologyInputProductTypeDD = technologyInputProductType.getDataDefinition();
            } else {
                this.technologyInputProductTypeId = null;
                this.technologyInputProductTypeDD = null;
            }
            Entity attribute = operationProductComponent
                    .getBelongsToField(OperationProductInComponentFields.ATTRIBUTE);
            if (Objects.nonNull(attribute)) {
                this.attributeId = attribute.getId();
                this.attributeDD = attribute.getDataDefinition();
            } else {
                this.attributeId = null;
                this.attributeDD = null;
            }

        } else {
            this.technologyInputProductTypeId = null;
            this.technologyInputProductTypeDD = null;
            this.attributeId = null;
            this.attributeDD = null;
        }
        this.operationProductComponentId = operationProductComponent.getId();
        this.operationProductComponentDD = operationProductComponent.getDataDefinition();
    }

    public OperationProductComponentHolder(final Entity product, final Entity technologyOperationComponent, final Entity productInputType,
                                           final Entity attribute,
                                           final Entity productionCountingQuantity, final OperationProductComponentEntityType entityType,
                                           final ProductMaterialType productMaterialType) {

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
        this.productMaterialType = productMaterialType;
        this.productionCountingQuantityId = (productionCountingQuantity == null) ? null : productionCountingQuantity.getId();

        this.technologyInputProductTypeId = Objects.isNull(productInputType) ? null : productInputType.getId();
        this.technologyInputProductTypeDD = Objects.isNull(productInputType) ? null : productInputType.getDataDefinition();

        this.attributeId =  Objects.isNull(attribute) ? null : attribute.getId();
        this.attributeDD = Objects.isNull(attribute) ? null : attribute.getDataDefinition();

        this.operationProductComponentId = null;
        this.operationProductComponentDD = null;

        if (entityType.getStringValue().equals(
                OperationProductComponentEntityType.OPERATION_PRODUCT_IN_COMPONENT.getStringValue())) {
            this.waste = false;

        } else {
            this.waste = ProductMaterialType.WASTE.equals(productMaterialType);
        }
    }

    public OperationProductComponentHolder(final Long productId, final Long technologyOperationComponentId,
                                           final DataDefinition productDD, final DataDefinition technologyOperationComponentDD,
                                           final OperationProductComponentEntityType entityType) {
        this.productId = productId;
        this.technologyOperationComponentId = technologyOperationComponentId;
        this.productDD = productDD;
        this.technologyOperationComponentDD = technologyOperationComponentDD;
        this.entityType = entityType;
        this.productMaterialType = ProductMaterialType.NONE;
        this.productionCountingQuantityId = null;
        this.operationProductComponentId = null;
        this.operationProductComponentDD = null;
        this.technologyInputProductTypeId = null;
        this.technologyInputProductTypeDD = null;
        this.attributeId = null;
        this.attributeDD = null;
        this.waste = false;
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

    public Entity getAttribute() {
        if ((attributeId == null) || (attributeDD == null)) {
            return null;
        } else {
            return attributeDD.get(attributeId);
        }
    }

    public Entity getTechnologyInputProductType() {
        if ((technologyInputProductTypeId == null) || (technologyInputProductTypeDD == null)) {
            return null;
        } else {
            return technologyInputProductTypeDD.get(technologyInputProductTypeId);
        }
    }

    public Entity getOperationProductComponent() {
        if ((getOperationProductComponentId() == null) || (getOperationProductComponentDD() == null)) {
            return null;
        } else {
            return getOperationProductComponentDD().get(getOperationProductComponentId());
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

    public ProductMaterialType getProductMaterialType() {
        return productMaterialType;
    }

    public Long getProductionCountingQuantityId() {
        return productionCountingQuantityId;
    }

    public DataDefinition getOperationProductComponentDD() {
        return operationProductComponentDD;
    }

    public Boolean isWaste() {
        return waste;
    }

    @Override
    public int hashCode() {
        if (productId != null) {
            return new HashCodeBuilder().append(productId).append(technologyOperationComponentId).append(entityType)
                    .append(technologyInputProductTypeId).toHashCode();
        } else {
            return new HashCodeBuilder().append(operationProductComponentId).append(technologyOperationComponentId)
                    .append(entityType).append(technologyInputProductTypeId).toHashCode();
        }
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

        if (productId != null) {
            return new EqualsBuilder().append(productId, other.productId)
                    .append(technologyOperationComponentId, other.technologyOperationComponentId)
                    .append(entityType, other.entityType)
                    .append(technologyInputProductTypeId, other.technologyInputProductTypeId).isEquals();
        } else {
            return new EqualsBuilder().append(operationProductComponentId, other.operationProductComponentId)
                    .append(technologyOperationComponentId, other.technologyOperationComponentId)
                    .append(entityType, other.entityType)
                    .append(technologyInputProductTypeId, other.technologyInputProductTypeId).isEquals();
        }
    }

    public Long getOperationProductComponentId() {
        return operationProductComponentId;
    }
}
