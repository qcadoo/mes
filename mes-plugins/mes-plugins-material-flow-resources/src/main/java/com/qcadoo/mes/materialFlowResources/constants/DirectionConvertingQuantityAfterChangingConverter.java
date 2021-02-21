package com.qcadoo.mes.materialFlowResources.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

import org.apache.commons.lang3.StringUtils;

public enum DirectionConvertingQuantityAfterChangingConverter {

    FROM_BASIC_TO_ADDITIONAL("01fromBasicToAdditional"), FROM_ADDITIONAL_TO_BASIC("02fromAdditionalToBasicQuantity");

    private final String value;

    private DirectionConvertingQuantityAfterChangingConverter(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static DirectionConvertingQuantityAfterChangingConverter of(final Entity entity) {
        Preconditions.checkArgument(entity != null, "Passed entity have to be non null");
        return parseString(entity.getStringField("directionConvertingQuantityAfterChangingConverter"));
    }

    public static DirectionConvertingQuantityAfterChangingConverter parseString(final String type) {
        for (DirectionConvertingQuantityAfterChangingConverter state : DirectionConvertingQuantityAfterChangingConverter.values()) {
            if (StringUtils.equalsIgnoreCase(type, state.getStringValue())) {
                return state;
            }
        }

        throw new IllegalArgumentException("Couldn't parse DirectionConvertingQuantityAfterChangingConverter from string '" + type + "'");
    }
}
