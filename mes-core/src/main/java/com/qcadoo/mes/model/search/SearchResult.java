package com.qcadoo.mes.model.search;

import java.util.List;

import com.qcadoo.mes.api.Entity;

/**
 * SearchResult contains list of entities, total number of entities and the search criteria used for produce this search result.
 * 
 * @apiviz.owns com.qcadoo.mes.api.Entity.
 * @apiviz.has com.qcadoo.mes.model.search.SearchCriteria
 */
public interface SearchResult {

    /**
     * Return list of entities matching given criteria.
     * 
     * @return list of entities
     */
    List<Entity> getEntities();

    /**
     * Return search criteria used for produce this search result.
     * 
     * @return criteria used for produce this result
     */
    SearchCriteria getCriteria();

    /**
     * Return total number of matching entities.
     * 
     * @return total number of matching entities
     */
    int getTotalNumberOfEntities();

}
