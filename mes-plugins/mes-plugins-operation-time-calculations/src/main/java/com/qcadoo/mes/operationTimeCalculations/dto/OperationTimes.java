package com.qcadoo.mes.operationTimeCalculations.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.model.api.Entity;

public class OperationTimes {

    private final Entity operation;

    private final OperationWorkTime times;

    public OperationTimes(final Entity operation, final OperationWorkTime workTimes) {
        this.operation = operation;
        this.times = workTimes;
    }

    public Entity getOperation() {
        return operation;
    }

    public OperationWorkTime getTimes() {
        return times;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(operation).append(times).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof OperationTimes)) {
            return false;
        }
        OperationTimes other = (OperationTimes) obj;
        return new EqualsBuilder().append(operation, other.operation).append(times, other.times).isEquals();
    }

}
