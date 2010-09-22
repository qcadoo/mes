package com.qcadoo.mes.core.data.internal.search;

import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.view.elements.GridComponent;

public interface SearchCriteriaBuilder {

    SearchResult list();

    SearchCriteriaBuilder forGrid(GridComponent gridDefinition);

    SearchCriteriaBuilder restrictedWith(Restriction restriction);

    SearchCriteriaBuilder orderBy(Order order);

    SearchCriteriaBuilder withMaxResults(int maxResults);

    SearchCriteriaBuilder withFirstResult(int firstResult);

}