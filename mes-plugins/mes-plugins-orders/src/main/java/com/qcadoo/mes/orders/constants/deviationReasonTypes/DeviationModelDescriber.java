package com.qcadoo.mes.orders.constants.deviationReasonTypes;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DeviationModelDescriber {

    private final String modelPlugin;

    private final String modelName;

    private final String reasonTypeFieldName;

    public DeviationModelDescriber(final String modelPlugin, final String modelName, final String reasonTypeFieldName) {
        this.modelPlugin = modelPlugin;
        this.modelName = modelName;
        this.reasonTypeFieldName = reasonTypeFieldName;
    }

    public String getModelPlugin() {
        return modelPlugin;
    }

    public String getModelName() {
        return modelName;
    }

    public String getReasonTypeFieldName() {
        return reasonTypeFieldName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DeviationModelDescriber rhs = (DeviationModelDescriber) obj;
        return new EqualsBuilder().append(this.modelPlugin, rhs.modelPlugin).append(this.modelName, rhs.modelName)
                .append(this.reasonTypeFieldName, rhs.reasonTypeFieldName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelPlugin).append(modelName).append(reasonTypeFieldName).toHashCode();
    }
}
