package com.qcadoo.mes.core.internal;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.internal.model.InternalDataDefinition;
import com.qcadoo.mes.core.search.SearchCriteria;
import com.qcadoo.mes.core.search.SearchResult;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.beans.Entity
 * @apiviz.uses com.qcadoo.mes.core.data.search.SearchCriteria
 * @apiviz.uses com.qcadoo.mes.core.data.search.ResultSet
 */
public interface DataAccessService {

    Entity save(InternalDataDefinition dataDefinition, Entity entity);

    Entity get(InternalDataDefinition dataDefinition, Long entityId);

    void delete(InternalDataDefinition dataDefinition, Long... entityId);

    SearchResult find(SearchCriteria searchCriteria);

    void moveTo(InternalDataDefinition dataDefinition, Long entityId, int position);

    void move(InternalDataDefinition dataDefinition, Long entityId, int offset);
}
