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

    Entity get(InternalDataDefinition dataDefinition, Long entityId);

    void delete(InternalDataDefinition dataDefinition, Long... entityId);

    void deleteHard(InternalDataDefinition dataDefinition, Long... entityId);

    SearchResult find(SearchCriteria searchCriteria);

    void moveTo(InternalDataDefinition dataDefinition, Long entityId, int position);

    void move(InternalDataDefinition dataDefinition, Long entityId, int offset);
}
