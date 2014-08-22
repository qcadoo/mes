package com.qcadoo.mes.assignmentToShift.dataProviders;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.base.Optional;
import com.qcadoo.model.api.search.SearchCriterion;

public class AssignmentToShiftCriteria {

    private Optional<SearchCriterion> shiftCriteria = Optional.absent();

    private Optional<SearchCriterion> criteria = Optional.absent();

    public static AssignmentToShiftCriteria empty() {
        return new AssignmentToShiftCriteria();
    }

    private AssignmentToShiftCriteria() {
    }

    public AssignmentToShiftCriteria withCriteria(final SearchCriterion assignmentCriteria) {
        this.criteria = Optional.fromNullable(assignmentCriteria);
        return this;
    }

    public AssignmentToShiftCriteria withShiftCriteria(final SearchCriterion shiftCriteria) {
        this.shiftCriteria = Optional.fromNullable(shiftCriteria);
        return this;
    }

    public Optional<SearchCriterion> getShiftCriteria() {
        return shiftCriteria;
    }

    public Optional<SearchCriterion> getCriteria() {
        return criteria;
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
        AssignmentToShiftCriteria rhs = (AssignmentToShiftCriteria) obj;
        return new EqualsBuilder().append(this.shiftCriteria, rhs.shiftCriteria).append(this.criteria, rhs.criteria).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(shiftCriteria).append(criteria).toHashCode();
    }
}
