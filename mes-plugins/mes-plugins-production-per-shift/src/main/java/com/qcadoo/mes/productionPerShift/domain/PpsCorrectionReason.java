package com.qcadoo.mes.productionPerShift.domain;

import java.util.Objects;

public class PpsCorrectionReason {

    private final String reason;

    public PpsCorrectionReason(final String reason) {
        this.reason = reason;
    }

    public String get() {
        return reason;
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
        PpsCorrectionReason rhs = (PpsCorrectionReason) obj;
        return Objects.equals(reason, rhs.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(reason);
    }

    @Override
    public String toString() {
        return String.format("PpsCorrectionReason('%s')", get());
    }
}
