package com.qcadoo.mes.core.data.internal;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.DataDefinition;

@Service
public class DataDefinitionServiceImpl implements DataDefinitionService {

    @Override
    public void save(DataDefinition dataDefinition) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public DataDefinition get(String entityName) {
        return null;
    }

    @Override
    public void delete(String entityName) {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public List<DataDefinition> list() {
        throw new UnsupportedOperationException("implement me");
    }

}
