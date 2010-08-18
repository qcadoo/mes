package com.qcadoo.mes.core.data.internal.search;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

public class ResultSetImpl implements ResultSet {

    private SearchCriteria searchCriteria;

    private List<Entity> results;

    @Override
    public List<Entity> getResults() {
        return results;
    }

    public void setResults(final List<Entity> results) {
        this.results = results;
    }

    @Override
    public Map<String, Integer> getAggregations() {
        // TODO KRNA Auto-generated method stub
        return null;
    }

    @Override
    public SearchCriteria getCriteria() {
        return searchCriteria;
    }

    public void setCriteria(final SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    public int getTotalNumberOfEntities() {
        // TODO KRNA Auto-generated method stub
        return 0;
    }

}
