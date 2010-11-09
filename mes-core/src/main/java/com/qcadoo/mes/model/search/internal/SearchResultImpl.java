package com.qcadoo.mes.model.search.internal;

import java.util.List;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchResult;

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
