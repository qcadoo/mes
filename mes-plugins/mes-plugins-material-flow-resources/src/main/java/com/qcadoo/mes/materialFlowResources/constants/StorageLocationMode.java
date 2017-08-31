package com.qcadoo.mes.materialFlowResources.constants;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum StorageLocationMode {
    ALL("01all"), SELECTED("02selected");

    private final String value;

    private StorageLocationMode(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static StorageLocationMode of(final Entity entity) {
        Preconditions.checkArgument(entity != null, "Passed entity have to be non null");
        return parseString(entity.getStringField(StocktakingFields.STORAGE_LOCATION_MODE));
    }

    public static StorageLocationMode parseString(final String type) {
        for (StorageLocationMode storageLocationMode : StorageLocationMode.values()) {
            if (StringUtils.equalsIgnoreCase(type, storageLocationMode.getStringValue())) {
                return storageLocationMode;
            }
        }

        throw new IllegalArgumentException("Couldn't parse storageLocationMode from string '" + type + "'");
    }

}
