package com.qcadoo.mes.model.search;

import java.util.Set;

import com.qcadoo.mes.model.DataDefinition;

/**
 * Object represents the criteria for listing entities. Together with definition - {@link com.qcadoo.mes.model.DataDefinition} -
 * and grip - optionally {@link com.qcadoo.mes.view.components.GridComponent} - it is used for building SQL query.
 * 
 * Order can be build only using orderable fields - {@link com.qcadoo.mes.model.types.FieldType#isOrderable()}.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.search.Restriction
 * @apiviz.has com.qcadoo.mes.core.data.search.Order
 */
public interface SearchCriteria {

    int getMaxResults();

    int getFirstResult();

    boolean isIncludeDeleted();

    Order getOrder();

    Set<Restriction> getRestrictions();

    DataDefinition getDataDefinition();

}
