package com.qcadoo.mes.core.data.internal.search;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.search.SearchCriteria;

public final class SearchResultImpl implements SearchResult {

    private SearchCriteria searchCriteria;

    private List<Entity> results;

    private int totalNumberOfEntities;

    @Override
    public List<Entity> getEntities() {
        return results;
    }

    public void setResults(final List<Entity> results) {
        this.results = results;
    }

    @Override
    public Map<String, Integer> getAggregations() {
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
        return totalNumberOfEntities;
    }

    public void setTotalNumberOfEntities(final int totalNumberOfEntities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
    }

}
