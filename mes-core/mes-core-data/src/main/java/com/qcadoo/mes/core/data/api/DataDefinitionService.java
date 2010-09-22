package com.qcadoo.mes.core.data.api;

import java.util.List;

import com.qcadoo.mes.core.data.model.DataDefinition;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.DataDefinition
 */
public interface DataDefinitionService {

    void save(DataDefinition dataDefinition);

    DataDefinition get(String entityName);

    void delete(String entityName);

    List<DataDefinition> list();

}
