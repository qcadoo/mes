package com.qcadoo.mes.core.data.search;

import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.view.elements.grid.GridDefinition;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaImpl;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.search.Restriction
 * @apiviz.uses com.qcadoo.mes.core.data.search.Order
 * @apiviz.uses com.qcadoo.mes.core.data.search.SearchCriteria
 */
public final class SearchCriteriaBuilder {

    private final SearchCriteriaImpl searchCriteria;

    private SearchCriteriaBuilder(final DataDefinition dataDefinition) {
        searchCriteria = new SearchCriteriaImpl(dataDefinition);
    }

    public SearchCriteriaBuilder forGrid(final GridDefinition gridDefinition) {
        searchCriteria.setGridDefinition(gridDefinition);
        return this;
    }

    public SearchCriteria build() {
        return searchCriteria;
    }

    public SearchCriteriaBuilder restrictedWith(final Restriction restriction) {
        searchCriteria.addRestriction(restriction);
        return this;
    }

    public SearchCriteriaBuilder orderBy(final Order order) {
        searchCriteria.setOrder(order);
        return this;
    }

    public SearchCriteriaBuilder withMaxResults(final int maxResults) {
        searchCriteria.setMaxResults(maxResults);
        return this;
    }

    public SearchCriteriaBuilder withFirstResult(final int firstResult) {
        searchCriteria.setFirstResult(firstResult);
        return this;
    }

    public static SearchCriteriaBuilder forEntity(final DataDefinition dataDefinition) {
        return new SearchCriteriaBuilder(dataDefinition);
    }

}
