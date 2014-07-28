package com.qcadoo.mes.technologies.tree.dataProvider;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
public class TechnologyOperationDataProvider {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Optional<Entity> findRoot(final long technologyId) {
        SearchCriteriaBuilder scb = getTocDataDefinition().find();
        scb.createCriteria(TechnologyOperationComponentFields.TECHNOLOGY, "tech_alias", JoinType.INNER).add(idEq(technologyId));
        scb.add(isNull(TechnologyOperationComponentFields.PARENT));
        return Optional.fromNullable(scb.setMaxResults(1).uniqueResult());
    }

    private DataDefinition getTocDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

    public long count(final SearchCriterion criteria) {
        SearchCriteriaBuilder scb = getTocDataDefinition().find();
        if (criteria != null) {
            scb.add(criteria);
        }
        scb.setProjection(alias(rowCount(), "cnt"));
        scb.addOrder(asc("cnt"));
        return FluentOptional.fromNullable(scb.setMaxResults(1).uniqueResult())
                .flatMap(EntityUtils.<Long> getSafeFieldExtractor("cnt")).or(0L);
    }
}
