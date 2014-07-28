package com.qcadoo.mes.productionPerShift.dataProvider;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchRestrictions.and;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.qcadoo.commons.functional.BiFunction;
import com.qcadoo.commons.functional.Fold;
import com.qcadoo.mes.orders.constants.TechnologyFieldsO;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityOpResult;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;

@Service
public class ProgressForDayDataProvider {

    public static final SearchOrder[] DEFAULT_SEARCH_ORDER = new SearchOrder[] { asc(ProgressForDayFields.DAY), asc("id") };

    private static final List<String> MODEL_PATH_TO_ORDER = ImmutableList.of(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT,
            TechnologyOperationComponentFields.TECHNOLOGY, TechnologyFieldsO.ORDERS);

    private static final BiFunction<SearchCriteriaBuilder, String, SearchCriteriaBuilder> CREATE_SUB_QUERY = new BiFunction<SearchCriteriaBuilder, String, SearchCriteriaBuilder>() {

        @Override
        public SearchCriteriaBuilder apply(final SearchCriteriaBuilder acc, final String fieldName) {
            return acc.createCriteria(fieldName, fieldName + "_alias", JoinType.INNER);
        }
    };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> findForOrder(final Entity order, final SearchOrder... searchOrders) {
        SearchCriteriaBuilder pfdCriteriaBuilder = getPfdDataDefinition().find();
        subCriteriaFor(pfdCriteriaBuilder, MODEL_PATH_TO_ORDER).add(idEq(order.getId()));
        for (SearchOrder searchOrder : searchOrders) {
            pfdCriteriaBuilder.addOrder(searchOrder);
        }
        return pfdCriteriaBuilder.list().getEntities();
    }

    public List<Entity> findForOperation(final Entity technologyOperation, final ProgressType progressType) {
        return findForOperation(technologyOperation, progressType == ProgressType.CORRECTED);
    }

    public List<Entity> findForOperation(final Entity technologyOperation, final boolean hasCorrections) {
        return find(
                and(eq(ProgressForDayFields.CORRECTED, hasCorrections),
                        belongsTo(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperation)),
                ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER);
    }

    public List<Entity> find(final SearchCriterion criteria, final SearchOrder... searchOrders) {
        SearchCriteriaBuilder pfdCriteriaBuilder = getPfdDataDefinition().find();
        if (criteria != null) {
            pfdCriteriaBuilder.add(criteria);
        }
        for (SearchOrder searchOrder : searchOrders) {
            pfdCriteriaBuilder.addOrder(searchOrder);
        }
        return pfdCriteriaBuilder.list().getEntities();
    }

    public EntityOpResult delete(final Iterable<Long> ids) {
        if (Iterables.isEmpty(ids)) {
            return EntityOpResult.successfull();
        }
        return getPfdDataDefinition().delete(FluentIterable.from(ids).toArray(Long.class));
    }

    private SearchCriteriaBuilder subCriteriaFor(final SearchCriteriaBuilder scb, final List<String> path) {
        return Fold.fold(path, scb, CREATE_SUB_QUERY);
    }

    private DataDefinition getPfdDataDefinition() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
    }

}
