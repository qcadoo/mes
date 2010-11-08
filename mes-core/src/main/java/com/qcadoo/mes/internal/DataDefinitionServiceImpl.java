package com.qcadoo.mes.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.aop.internal.Monitorable;

@Service
public final class DataDefinitionServiceImpl implements DataDefinitionService {

    private final Map<String, DataDefinition> dataDefinitions = new HashMap<String, DataDefinition>();

    @Override
    @Monitorable
    public void save(final DataDefinition dataDefinition) {
        dataDefinitions.put(dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName(), dataDefinition);
    }

    @Override
    @Monitorable
    public DataDefinition get(final String pluginIdentifier, final String modelName) {
        DataDefinition dataDefinition = dataDefinitions.get(pluginIdentifier + "." + modelName);
        checkNotNull(dataDefinition, "data definition for %s#%s cannot be found", pluginIdentifier, modelName);
        return dataDefinition;
    }

    @Override
    @Monitorable
    public void delete(final DataDefinition dataDefinition) {
        dataDefinitions.remove(dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName());
    }

    @Override
    @Monitorable
    public List<DataDefinition> list() {
        return new ArrayList<DataDefinition>(dataDefinitions.values());
    }

}
