package com.qcadoo.mes.api;

import java.util.List;

import com.qcadoo.mes.model.DataDefinition;

/**
 * Service for manipulating data definitions.
 * 
 * @apiviz.uses com.qcadoo.mes.model.DataDefinition
 */
public interface DataDefinitionService {

    /**
     * Return the data definition matching the given plugin's identifier and model's name.
     * 
     * @param pluginIdentifier
     *            plugin's identifier
     * @param modelName
     *            model's name
     * @return the data definition
     * @throws NullPointerException
     *             if data definition is not found
     */
    DataDefinition get(String pluginIdentifier, String modelName);

    /**
     * Return all defined data definitions.
     * 
     * @return the data definitions
     */
    List<DataDefinition> list();

    /**
     * Save the data definition.
     * 
     * @param dataDefinition
     *            data definition
     */
    void save(DataDefinition dataDefinition);

    /**
     * Delete the data definition.
     * 
     * @param dataDefinition
     *            data definition
     */
    void delete(DataDefinition dataDefinition);

}
