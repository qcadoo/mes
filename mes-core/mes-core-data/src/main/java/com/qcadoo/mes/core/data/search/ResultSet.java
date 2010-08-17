package com.qcadoo.mes.core.data.search;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;

/**
 * ResultSet contains list of entities, counted aggregations, total number of entities and the criteria used for produce this
 * result set.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.beans.Entity
 * @apiviz.has com.qcadoo.mes.core.data.search.SearchCriteria
 */
public interface ResultSet {

    List<Entity> getResults();

    Map<String, Integer> getAggregations();

    SearchCriteria getCriteria();

    int getTotalNumberOfEntities();

}
