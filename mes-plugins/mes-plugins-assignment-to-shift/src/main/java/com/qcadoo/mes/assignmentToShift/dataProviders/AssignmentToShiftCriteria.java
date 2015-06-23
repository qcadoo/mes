/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.assignmentToShift.dataProviders;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
