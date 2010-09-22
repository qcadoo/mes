package com.qcadoo.mes.core.data.search;

import java.util.Set;

import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.view.elements.GridComponent;

/**
 * Object represents the criteria for listing entities. Together with definition -
 * {@link com.qcadoo.mes.core.data.model.DataDefinition} - and grip - optionally
 * {@link com.qcadoo.mes.core.data.view.elements.GridComponent} - it is used for building SQL query.
 * 
 * Order can be build only using orderable fields - {@link com.qcadoo.mes.core.data.types.FieldType#isOrderable()}.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.search.Restriction
 * @apiviz.has com.qcadoo.mes.core.data.search.Order
 */
public interface SearchCriteria {

    int getMaxResults();

    int getFirstResult();

    Order getOrder();

    Set<Restriction> getRestrictions();

    DataDefinition getDataDefinition();

    GridComponent getGridDefinition();

}
