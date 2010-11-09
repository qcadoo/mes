package com.qcadoo.mes.model;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.validators.EntityValidator;

/**
 * Object defines database structure. The {@link #getPluginIdentifier()} and {@link #getName()} are used to calculate table name.
 * 
 * @apiviz.owns com.qcadoo.mes.model.FieldDefinition
 * @apiviz.owns com.qcadoo.mes.model.validators.EntityValidator
 * @apiviz.uses com.qcadoo.mes.model.search.SearchCriteriaBuilder
 */
public interface DataDefinition {

    /**
     * Return name of this data definition.
     * 
     * @return name
     */
    String getName();

    /**
     * Return plugin's identifier for this data definition.
     * 
     * @return plugin's identifier
     */
    String getPluginIdentifier();

    /**
     * Return the entity related with this data definition, by its id.
     * 
     * @param id
     *            id
     * @return entity
     */
    Entity get(final Long id);

    /**
     * Mark as deleted the entity related with this data definition, by its id.
     * 
     * @param id
     *            id
     */
    void delete(final Long id);

    /**
     * Delete the entity related with this data definition, by its id.
     * 
     * @param id
     *            id
     */
    void deleteHard(final Long id);

    /**
     * Save the entity related with this data definition.
     * 
     * @param entity
     *            entity to save
     * @return saved entity
     */
    Entity save(final Entity entity);

    /**
     * Create search criteria builder for this data definition.
     * 
     * @return new search criteria builder
     */
    SearchCriteriaBuilder find();

    /**
     * Move the prioritizable entity by offset.
     * 
     * @param id
     *            id
     * @param offset
     *            offset
     */
    void move(final Long id, final int offset);

    /**
     * Move the prioritizable entity to the target position.
     * 
     * @param id
     *            id
     * @param position
     *            position
     */
    void moveTo(final Long id, final int position);

    /**
     * Return all defined fields' definitions.
     * 
     * @return fields' definitions
     */
    Map<String, FieldDefinition> getFields();

    /**
     * Return field definition by its name.
     * 
     * @param fieldName
     *            field's name
     * @return field's definition
     */
    FieldDefinition getField(final String fieldName);

    /**
     * Return priority field's definition.
     * 
     * @return priority field's definion, null if entity is not prioritizable
     */
    FieldDefinition getPriorityField();

    /**
     * Return all defined entity's validators.
     * 
     * @return entity's validators
     */
    List<EntityValidator> getValidators();

    /**
     * Return true if entity is prioritizable.
     * 
     * @return true if entity is prioritizable
     */
    boolean isPrioritizable();

}
