package com.qcadoo.mes.plugins.products.data.mock;

import java.util.Set;

import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.SearchCriteria;

public class SearchCriteriaMock implements SearchCriteria {

    private String name;

    public int maxResults;

    public int firstResult;

    public SearchCriteriaMock(String name, int maxResults, int firstResult) {
        this.name = name;
        this.maxResults = maxResults;
        this.firstResult = firstResult;
    }

    @Override
    public String getEntityName() {
        return name;
    }

    @Override
    public String getGridName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public Order getOrder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Restriction> getRestrictions() {
        // TODO Auto-generated method stub
        return null;
    }

}
