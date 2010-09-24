package com.qcadoo.mes.core.search;

import com.qcadoo.mes.core.view.elements.GridComponent;

public interface SearchCriteriaBuilder {

    SearchResult list();

    SearchCriteriaBuilder forGrid(GridComponent gridDefinition);

    SearchCriteriaBuilder restrictedWith(Restriction restriction);

    SearchCriteriaBuilder orderBy(Order order);

    SearchCriteriaBuilder withMaxResults(int maxResults);

    SearchCriteriaBuilder withFirstResult(int firstResult);

}
