package com.qcadoo.mes.core.data.api;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.beans.Entity
 * @apiviz.uses com.qcadoo.mes.core.data.search.SearchCriteria
 * @apiviz.uses com.qcadoo.mes.core.data.search.ResultSet
 */
public interface DataAccessService {

    void save(String entityName, Entity entity);

    Entity get(String entityName, Long entityId);

    void delete(String entityName, Long entityId);

    ResultSet find(String entityName, SearchCriteria searchCriteria);
}
