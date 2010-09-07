package com.qcadoo.mes.core.data.api;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.validation.ValidationResults;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.beans.Entity
 * @apiviz.uses com.qcadoo.mes.core.data.search.SearchCriteria
 * @apiviz.uses com.qcadoo.mes.core.data.search.ResultSet
 */
public interface DataAccessService {

    ValidationResults save(String entityName, Entity entity);

    Entity get(String entityName, Long entityId);

    void delete(String entityName, Long... entityId);

    SearchResult find(String entityName, SearchCriteria searchCriteria);

    void moveTo(String entityName, Long entityId, int position);

    void move(String entityName, Long entityId, int offset);
}
