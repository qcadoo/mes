package com.qcadoo.mes.internal;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.internal.InternalDataDefinition;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchResult;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.beans.Entity
 * @apiviz.uses com.qcadoo.mes.core.data.search.SearchCriteria
 * @apiviz.uses com.qcadoo.mes.core.data.search.ResultSet
 */
public interface DataAccessService {

    Entity save(InternalDataDefinition dataDefinition, Entity entity);

    /**
     * Return the entity related with this data definition, by its id.
     * 
     * @param id
     *            id
     * @return entity
     */
    Entity get(InternalDataDefinition dataDefinition, Long entityId);

    /**
     * Mark as deleted the entity related with this data definition, by its id.
     * 
     * @param id
     *            id
     */
    void delete(InternalDataDefinition dataDefinition, Long... entityId);

    /**
     * Delete the entity related with this data definition, by its id.
     * 
     * @param id
     *            id
     */
    void deleteHard(InternalDataDefinition dataDefinition, Long... entityId);

    /**
     * Save the entity related with this data definition.
     * 
     * @param entity
     *            entity to save
     * @return saved entity
     */
    SearchResult find(SearchCriteria searchCriteria);

    /**
     * Move the prioritizable entity to the target position.
     * 
     * @param id
     *            id
     * @param position
     *            position
     */
    void moveTo(InternalDataDefinition dataDefinition, Long entityId, int position);

    /**
     * Move the prioritizable entity by offset.
     * 
     * @param id
     *            id
     * @param offset
     *            offset
     */
    void move(InternalDataDefinition dataDefinition, Long entityId, int offset);
}
