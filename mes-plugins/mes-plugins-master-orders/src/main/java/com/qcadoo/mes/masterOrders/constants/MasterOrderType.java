package com.qcadoo.mes.masterOrders.constants;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum MasterOrderType {

    UNDEFINED("01undefined"), ONE_PRODUCT("02oneProduct"), MANY_PRODUCTS("03manyProducts");

    private final String masterOrderType;

    private MasterOrderType(final String masterOrderType) {
        this.masterOrderType = masterOrderType;
    }

    public String getStringValue() {
        return masterOrderType;
    }

    public static MasterOrderType of(final Entity masterOrderEntity) {
        Preconditions.checkArgument(masterOrderEntity != null, "Missing entity");
        return parseString(masterOrderEntity.getStringField(MasterOrderFields.MASTER_ORDER_TYPE));
    }

    public static MasterOrderType parseString(final String rawMasterOrderType) {
        if (StringUtils.isBlank(rawMasterOrderType)) {
            return UNDEFINED;
        }

        String masterOrderType = StringUtils.trim(rawMasterOrderType);
        for (MasterOrderType type : values()) {
            if (StringUtils.equalsIgnoreCase(type.getStringValue(), masterOrderType)) {
                return type;
            }
        }

        throw new IllegalStateException("Unsupported masterOrderType: " + masterOrderType);
    }

}
