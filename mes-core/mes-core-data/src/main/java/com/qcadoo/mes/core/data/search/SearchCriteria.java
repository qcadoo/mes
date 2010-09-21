package com.qcadoo.mes.core.data.search;

import java.util.Set;

import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.view.elements.grid.GridDefinition;

/**
 * Object represents the criteria for listing entities. Together with definition -
 * {@link com.qcadoo.mes.core.data.model.ModelDefinition} - and grip - optionally
 * {@link com.qcadoo.mes.core.data.view.elements.grid.GridDefinition} - it is used for building SQL query.
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

    ModelDefinition getDataDefinition();

    GridDefinition getGridDefinition();

}
