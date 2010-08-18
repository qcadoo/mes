package com.qcadoo.mes.plugins.products.data.mock;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

@Component
public class DataAccessServiceMock implements DataAccessService {

    @Override
    public void save(String entityName, Entity entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public Entity get(String entityName, Long entityId) {
        // TODO Auto-generated method stub
        return null;
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
