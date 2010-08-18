package com.qcadoo.mes.plugins.products.data.mock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

public class ResultSetMock implements ResultSet {

    private SearchCriteria criteria;

    public ResultSetMock(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public List<Entity> getResults() {
        List<Entity> entities = new LinkedList<Entity>();
        for (int i = criteria.getFirstResult(); i < criteria.getFirstResult() + criteria.getMaxResults(); i++) {
            Map<String, Object> entityFields = new HashMap<String, Object>();
            entityFields.put("Numer", "" + i);
            entityFields.put("Nazwa", "produkt " + i);
            entityFields.put("Typ materialu", "material");
            entityFields.put("Kod EAN", "ean");
            Entity entity = new Entity((long) i, entityFields);
            entities.add(entity);
        }
        return entities;
    }

    @Override
    public Map<String, Integer> getAggregations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchCriteria getCriteria() {
        return criteria;
    }

    @Override
    public int getTotalNumberOfEntities() {
        // TODO Auto-generated method stub
        return criteria.getMaxResults();
    }

}
