package com.qcadoo.mes.core.data.internal.search;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.view.elements.grid.GridDefinition;

public final class SearchCriteriaImpl implements SearchCriteria, SearchCriteriaBuilder {

    private static final int DEFAULT_MAX_RESULTS = 25;

    private static final int MAX_RESTRICTIONS = 5;

    private int maxResults = DEFAULT_MAX_RESULTS;

    private int firstResult = 0;

    private Order order;

    private final Set<Restriction> restrictions = new HashSet<Restriction>();

    private final ModelDefinition dataDefinition;

    private GridDefinition gridDefinition;

    public SearchCriteriaImpl(final ModelDefinition dataDefinition) {
        checkNotNull(dataDefinition);
        this.dataDefinition = dataDefinition;
        if (dataDefinition.isPrioritizable()) {
            order = Order.asc(dataDefinition.getPriorityField().getName());
        } else {
            order = Order.asc();
        }
    }

    @Override
    public ModelDefinition getDataDefinition() {
        return dataDefinition;
    }

    @Override
    public GridDefinition getGridDefinition() {
        return gridDefinition;
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
    public SearchCriteriaBuilder forGrid(final GridDefinition gridDefinition) {
        this.gridDefinition = gridDefinition;
        return this;
    }

    @Override
    public SearchResult list() {
        return dataDefinition.find(this);
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
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("maxResults", maxResults).append("firstResult", firstResult)
                .append("order", order).append("restrictions", restrictions).toString();
    }

}
