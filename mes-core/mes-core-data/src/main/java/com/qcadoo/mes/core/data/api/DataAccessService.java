package com.qcadoo.mes.core.data.api;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchResult;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.beans.Entity
 * @apiviz.uses com.qcadoo.mes.core.data.search.SearchCriteria
 * @apiviz.uses com.qcadoo.mes.core.data.search.ResultSet
 */
public interface DataAccessService {

    Entity save(ModelDefinition dataDefinition, Entity entity);

    Entity get(ModelDefinition dataDefinition, Long entityId);

    void delete(ModelDefinition dataDefinition, Long... entityId);

    SearchResult find(SearchCriteria searchCriteria);

    void moveTo(ModelDefinition dataDefinition, Long entityId, int position);

    void move(ModelDefinition dataDefinition, Long entityId, int offset);
}
