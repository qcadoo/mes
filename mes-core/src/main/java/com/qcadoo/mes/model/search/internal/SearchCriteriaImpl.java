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

    private int firstResult = 0;

    private Order order;

    private final Set<Restriction> restrictions = new HashSet<Restriction>();

    private final DataDefinition dataDefinition;

    private boolean includeDeleted = false;

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
    public boolean isIncludeDeleted() {
        return includeDeleted;
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
    public SearchCriteriaBuilder orderBy(final Order order) {
        checkNotNull(order);
        this.order = order;
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
    public SearchCriteriaBuilder includeDeleted() {
        this.includeDeleted = true;
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
        return "SearchCriteria[maxResults=" + maxResults + ", firstResult=" + firstResult + ", order=" + order
                + ", includeDeleted=" + includeDeleted + ", restrictions=" + restrictions + "]";
    }

}
