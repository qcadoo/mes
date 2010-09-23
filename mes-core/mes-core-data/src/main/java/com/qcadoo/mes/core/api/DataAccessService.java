package com.qcadoo.mes.core.api;

import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.search.SearchCriteria;
import com.qcadoo.mes.core.search.SearchResult;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.beans.Entity
 * @apiviz.uses com.qcadoo.mes.core.data.search.SearchCriteria
 * @apiviz.uses com.qcadoo.mes.core.data.search.ResultSet
 */
public interface DataAccessService {

    Entity save(DataDefinition dataDefinition, Entity entity);

    Entity get(DataDefinition dataDefinition, Long entityId);

    void delete(DataDefinition dataDefinition, Long... entityId);

    SearchResult find(SearchCriteria searchCriteria);

    void moveTo(DataDefinition dataDefinition, Long entityId, int position);

    void move(DataDefinition dataDefinition, Long entityId, int offset);
}
