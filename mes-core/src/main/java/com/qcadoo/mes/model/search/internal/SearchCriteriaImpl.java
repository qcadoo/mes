/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.model.search.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.internal.InternalDataDefinition;
import com.qcadoo.mes.model.search.Order;
import com.qcadoo.mes.model.search.Restriction;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;

public final class SearchCriteriaImpl implements SearchCriteria, SearchCriteriaBuilder {

    private static final int DEFAULT_MAX_RESULTS = Integer.MAX_VALUE;

    private static final int MAX_RESTRICTIONS = 5;

    private int maxResults = DEFAULT_MAX_RESULTS;

    private String distinctProperty;

    private int firstResult = 0;

    private Order order;

    private final Set<Restriction> restrictions = new HashSet<Restriction>();

    private final DataDefinition dataDefinition;

    public SearchCriteriaImpl(final DataDefinition dataDefinition) {
        checkNotNull(dataDefinition);
        this.dataDefinition = dataDefinition;
        if (dataDefinition.getPriorityField() != null) {
            order = Order.asc(dataDefinition.getPriorityField().getName());
        } else {
            order = Order.asc();
        }
    }

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public Order getOrder() {
        return order;
    }

    @Override
    public Set<Restriction> getRestrictions() {
        return restrictions;
    }

    @Override
    public String getDistinctProperty() {
        return distinctProperty;
    }

    @Override
    public SearchResult list() {
        return ((InternalDataDefinition) dataDefinition).find(this);
    }

    @Override
    public SearchCriteriaBuilder restrictedWith(final Restriction restriction) {
        checkState(restrictions.size() < MAX_RESTRICTIONS, "too many restriction, max is %s", MAX_RESTRICTIONS);
        this.restrictions.add(restriction);
        return this;
    }

    @Override
    public SearchCriteriaBuilder orderAscBy(final String fieldName) {
        checkNotNull(fieldName);
        this.order = Order.asc(fieldName);
        return this;
    }

    @Override
    public SearchCriteriaBuilder orderDescBy(final String fieldName) {
        checkNotNull(fieldName);
        this.order = Order.desc(fieldName);
        return this;
    }

    @Override
    public SearchCriteriaBuilder withMaxResults(final int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public SearchCriteriaBuilder withFirstResult(final int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return "SearchCriteria[dataDefinition=" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName()
                + ", maxResults=" + maxResults + ", firstResult=" + firstResult + ", order=" + order + ", distinctProperty="
                + distinctProperty + ", restrictions=" + restrictions + "]";
    }

    @Override
    public void withDistinctProperty(final String distinctProperty) {
        this.distinctProperty = distinctProperty;
    }

}
