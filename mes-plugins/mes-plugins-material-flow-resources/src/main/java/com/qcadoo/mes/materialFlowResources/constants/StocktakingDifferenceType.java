package com.qcadoo.mes.materialFlowResources.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;

public enum StocktakingDifferenceType {

    SHORTAGE("01shortage"), SURPLUS("02surplus");

    private final String value;

    private StocktakingDifferenceType(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static StocktakingDifferenceType of(final Entity entity) {
        Preconditions.checkArgument(entity != null, "Passed entity have to be non null");
        return parseString(entity.getStringField(StocktakingDifferenceFields.TYPE));
    }

    public static StocktakingDifferenceType parseString(final String type) {
        for (StocktakingDifferenceType stocktakingDifferenceType : StocktakingDifferenceType.values()) {
            if (StringUtils.equalsIgnoreCase(type, stocktakingDifferenceType.getStringValue())) {
                return stocktakingDifferenceType;
            }
        }

        throw new IllegalArgumentException("Couldn't parse stocktakingDifferenceType from string '" + type + "'");
    }

}
