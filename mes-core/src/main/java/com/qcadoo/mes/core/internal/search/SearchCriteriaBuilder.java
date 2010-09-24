package com.qcadoo.mes.core.internal.search;

import com.qcadoo.mes.core.search.Order;
import com.qcadoo.mes.core.search.Restriction;
import com.qcadoo.mes.core.search.SearchResult;
import com.qcadoo.mes.core.view.elements.GridComponent;

public interface SearchCriteriaBuilder {

    SearchResult list();

    SearchCriteriaBuilder forGrid(GridComponent gridDefinition);

    SearchCriteriaBuilder restrictedWith(Restriction restriction);

    SearchCriteriaBuilder orderBy(Order order);

    SearchCriteriaBuilder withMaxResults(int maxResults);

    SearchCriteriaBuilder withFirstResult(int firstResult);

}
