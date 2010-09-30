package com.qcadoo.mes.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.model.DataDefinition;

@Service
public final class DataDefinitionServiceImpl implements DataDefinitionService {

    private final Map<String, DataDefinition> dataDefinitions = new HashMap<String, DataDefinition>();

    @Override
    public void save(final DataDefinition dataDefinition) {
        dataDefinitions.put(dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName(), dataDefinition);
    }

    @Override
    public DataDefinition get(final String pluginIdentifier, final String modelName) {
        DataDefinition dataDefinition = dataDefinitions.get(pluginIdentifier + "." + modelName);
        checkNotNull(dataDefinition, "data definition for %s#%s cannot be found", pluginIdentifier, modelName);
        return dataDefinition;
    }

    @Override
    public void delete(final String pluginIdentifier, final String modelName) {
        dataDefinitions.remove(pluginIdentifier + "." + modelName);
    }

    @Override
    public List<DataDefinition> list() {
        return new ArrayList<DataDefinition>(dataDefinitions.values());
    }

}
