package com.qcadoo.mes.core.data.api;

import java.util.List;

import com.qcadoo.mes.core.data.model.DataDefinition;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.DataDefinition
 */
public interface DataDefinitionService {

    void save(DataDefinition dataDefinition);

    DataDefinition get(String pluginIdentifier, String modelName);

    void delete(String pluginIdentifier, String modelName);

    List<DataDefinition> list();

}
