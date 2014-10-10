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
package com.qcadoo.mes.technologies.tree.traversing;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Optional;
import com.qcadoo.model.api.search.SearchCriterion;

public class MasterOutputProductCriteria {

    private Optional<SearchCriterion> tocCriteria = Optional.absent();

    private Optional<SearchCriterion> opocCriteria = Optional.absent();

    private Optional<SearchCriterion> prodCriteria = Optional.absent();

    private Optional<SearchCriterion> techCriteria = Optional.absent();

    private Optional<SearchCriterion> parentTocCriteria = Optional.absent();

    private Optional<SearchCriterion> parentOpicCriteria = Optional.absent();

    public static MasterOutputProductCriteria empty() {
        return new MasterOutputProductCriteria();
    }

    private MasterOutputProductCriteria() {
    }

    public Optional<SearchCriterion> getTocCriteria() {
        return tocCriteria;
    }

    public MasterOutputProductCriteria withTocCriteria(final SearchCriterion tocCriteria) {
        this.tocCriteria = Optional.fromNullable(tocCriteria);
        return this;
    }

    public Optional<SearchCriterion> getOpocCriteria() {
        return opocCriteria;
    }

    public MasterOutputProductCriteria withOpocCriteria(final SearchCriterion opocCriteria) {
        this.opocCriteria = Optional.fromNullable(opocCriteria);
        return this;
    }

    public Optional<SearchCriterion> getProdCriteria() {
        return prodCriteria;
    }

    public MasterOutputProductCriteria withProdCriteria(final SearchCriterion prodCriteria) {
        this.prodCriteria = Optional.fromNullable(prodCriteria);
        return this;
    }

    public Optional<SearchCriterion> getTechCriteria() {
        return techCriteria;
    }

    public MasterOutputProductCriteria withTechCriteria(final SearchCriterion techCriteria) {
        this.techCriteria = Optional.fromNullable(techCriteria);
        return this;
    }

    public Optional<SearchCriterion> getParentTocCriteria() {
        return parentTocCriteria;
    }

    public MasterOutputProductCriteria withParentTocCriteria(final SearchCriterion parentTocCriteria) {
        this.parentTocCriteria = Optional.fromNullable(parentTocCriteria);
        return this;
    }

    public Optional<SearchCriterion> getParentOpicCriteria() {
        return parentOpicCriteria;
    }

    public MasterOutputProductCriteria withParentOpicCriteria(final SearchCriterion parentOpicCriteria) {
        this.parentOpicCriteria = Optional.fromNullable(parentOpicCriteria);
        return this;
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
        MasterOutputProductCriteria rhs = (MasterOutputProductCriteria) obj;
        return new EqualsBuilder().append(this.tocCriteria, rhs.tocCriteria).append(this.opocCriteria, rhs.opocCriteria)
                .append(this.prodCriteria, rhs.prodCriteria).append(this.techCriteria, rhs.techCriteria)
                .append(this.parentTocCriteria, rhs.parentTocCriteria).append(this.parentOpicCriteria, rhs.parentOpicCriteria)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tocCriteria).append(opocCriteria).append(prodCriteria).append(techCriteria)
                .append(parentTocCriteria).append(parentOpicCriteria).toHashCode();
    }
}
