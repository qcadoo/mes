package com.qcadoo.mes.core.data.api;

import java.util.List;

import com.qcadoo.mes.core.data.model.ModelDefinition;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.DataDefinition
 */
public interface DataDefinitionService {

    void save(ModelDefinition dataDefinition);

    ModelDefinition get(String entityName);

    void delete(String entityName);

    List<ModelDefinition> list();

}
