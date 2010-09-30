package com.qcadoo.mes.model.search;


public interface SearchCriteriaBuilder {

    SearchResult list();

    SearchCriteriaBuilder restrictedWith(Restriction restriction);

    SearchCriteriaBuilder orderBy(Order order);

    SearchCriteriaBuilder withMaxResults(int maxResults);

    SearchCriteriaBuilder withFirstResult(int firstResult);

}
