package com.qcadoo.mes.plugins.products.data.mock;

import java.util.List;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.DataDefinition;

public class DataDefinitionServiceMock implements DataDefinitionService {

    public void save(DataDefinition dataDefinition) {

    }

    public DataDefinition get(String entityName) {
        if (!"product".equals(entityName)) {
            return null;
        }
        DataDefinition dataDef = new DataDefinitionMock("product");
        return dataDef;
    }

    public void delete(String entityName) {

    }

    public List<DataDefinition> list() {
        return null;
    }

}
