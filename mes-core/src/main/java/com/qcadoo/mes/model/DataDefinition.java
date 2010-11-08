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
 * @apiviz.uses com.qcadoo.mes.internal.DataAccessService
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
     * {@link com.qcadoo.mes.internal.DataAccessService#get(com.qcadoo.mes.model.internal.InternalDataDefinition, Long)}
     */
    Entity get(final Long id);

    /**
     * {@link com.qcadoo.mes.internal.DataAccessService#delete(com.qcadoo.mes.model.internal.InternalDataDefinition, Long...)}
     */
    void delete(final Long id);

    /**
     * {@link com.qcadoo.mes.internal.DataAccessService#deleteHard(com.qcadoo.mes.model.internal.InternalDataDefinition, Long...)}
     */
    void deleteHard(final Long id);

    /**
     * {@link com.qcadoo.mes.internal.DataAccessService#save(com.qcadoo.mes.model.internal.InternalDataDefinition, Entity)}
     */
    Entity save(final Entity entity);

    /**
     * Create search criteria builder for this data definition.
     * 
     * @return new search criteria builder
     */
    SearchCriteriaBuilder find();

    /**
     * {@link com.qcadoo.mes.internal.DataAccessService#move(com.qcadoo.mes.model.internal.InternalDataDefinition, Long, int)}
     */
    void move(final Long id, final int offset);

    /**
     * {@link com.qcadoo.mes.internal.DataAccessService#moveTo(com.qcadoo.mes.model.internal.InternalDataDefinition, Long, int)}
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
