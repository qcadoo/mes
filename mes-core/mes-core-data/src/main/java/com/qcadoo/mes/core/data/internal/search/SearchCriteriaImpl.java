package com.qcadoo.mes.core.data.internal.search;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.SearchCriteria;

public final class SearchCriteriaImpl implements SearchCriteria {

    private static final int DEFAULT_MAX_RESULTS = 25;

    private static final int MAX_RESTRICTIONS = 5;

    private final String entityName;

    private String gridName;

    private int maxResults = DEFAULT_MAX_RESULTS;

    private int firstResult = 0;

    private Order order = Order.asc();

    private Set<Restriction> restrictions = new HashSet<Restriction>();

    public SearchCriteriaImpl(final String entityName) {
        checkNotNull(entityName);
        checkArgument(isNotEmpty(entityName), "must not be empty");
        this.entityName = entityName;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public String getGridName() {
        return gridName;
    }

    public void setGridName(final String gridName) {
        this.gridName = gridName;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(final int maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(final int firstResult) {
        this.firstResult = firstResult;
    }

    @Override
    public Order getOrder() {
        return order;
    }

    public void setOrder(final Order order) {
        checkNotNull(order);
        this.order = order;
    }

    @Override
    public Set<Restriction> getRestrictions() {
        return restrictions;
    }

    public void addRestriction(final Restriction restriction) {
        checkNotNull(restriction);
        checkState(restrictions.size() < MAX_RESTRICTIONS, "too many restriction, max is %s", MAX_RESTRICTIONS);
        this.restrictions.add(restriction);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
