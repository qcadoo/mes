package com.qcadoo.mes.plugins.products.data.mock;

import java.util.HashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

public class DataAccessServiceMock implements DataAccessService {

    @Override
    public void save(String entityName, Entity entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public Entity get(String entityName, Long entityId) {
        Map<String, Object> entityFields = new HashMap<String, Object>();
        entityFields.put("Numer", entityId);
        entityFields.put("Nazwa", "produkt " + entityId);
        entityFields.put("Typ materialu", "produkt koncowy");
        entityFields.put("EAN", "0");
        entityFields.put("Kategoria", "niesklasyfikowany");
        entityFields.put("Jednostka", "szt");
        Entity entity = new Entity(entityId, entityFields);
        return entity;

    }

    @Override
    public void delete(String entityName, Long entityId) {
        // TODO Auto-generated method stub

    }

    @Override
    public ResultSet find(String entityName, SearchCriteria searchCriteria) {
        if (!"product".equals(entityName)) {
            return null;
        }
        ResultSet rs = new ResultSetMock(searchCriteria);
        return rs;
    }
}
