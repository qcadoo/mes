package com.qcadoo.mes.productionLines.helper;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.or;

import java.util.List;
import java.util.Set;

import com.qcadoo.mes.productionLines.ProductionLinesSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;

@Service
public class ProductionLinesSearchServiceImpl implements ProductionLinesSearchService {

    private static final String ID_ALIAS = "id";

    private static final String DOT_ID = ".id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Set<Long> findAllLines() {
        return findByTechOrTechGroup(LineSearchMode.ALL, null);
    }

    @Override
    public Set<Long> findLinesSupportingTechnology(final Long technologyId) {
        return findByTechOrTechGroup(LineSearchMode.SUPPORTS_TECHNOLOGY, technologyId);
    }

    @Override
    public Set<Long> findLinesSupportingTechnologyGroup(final Long technologyGroupId) {
        return findByTechOrTechGroup(LineSearchMode.SUPPORTS_TECHNOLOGY_GROUP, technologyGroupId);
    }

    private Set<Long> findByTechOrTechGroup(final LineSearchMode searchMode, final Long techOrTechGroupId) {
        return extractIds(findProjections(searchMode, techOrTechGroupId));
    }

    private List<Entity> findProjections(final LineSearchMode searchMode, final Long techOrTechGroupId) {
        SearchCriteriaBuilder scb = getProductionLineDataDef().find();
        scb.setProjection(alias(id(), "id"));
        searchMode.appendCriteria(scb, techOrTechGroupId);
        return scb.list().getEntities();
    }

    private Set<Long> extractIds(final Iterable<Entity> projections) {
        Set<Long> productionLineIds = Sets.newHashSet();
        for (Entity projection : projections) {
            productionLineIds.add((Long) projection.getField(ID_ALIAS));
        }
        return productionLineIds;
    }

    private DataDefinition getProductionLineDataDef() {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE);
    }

    private enum LineSearchMode {

        ALL {

            @Override
            public void appendCriteria(final SearchCriteriaBuilder scb, final Long id) {
                // DO NOTHING
            }
        },
        SUPPORTS_TECHNOLOGY {

            @Override
            public void appendCriteria(final SearchCriteriaBuilder scb, final Long id) {
                scb.createAlias(ProductionLineFields.TECHNOLOGIES, ProductionLineFields.TECHNOLOGIES, JoinType.LEFT);
                scb.add(or(SUPPORTS_ALL_CRITERION, matchesId(ProductionLineFields.TECHNOLOGIES, id)));
            }
        },
        SUPPORTS_TECHNOLOGY_GROUP {

            @Override
            public void appendCriteria(final SearchCriteriaBuilder scb, final Long id) {
                scb.createAlias(ProductionLineFields.GROUPS, ProductionLineFields.GROUPS, JoinType.LEFT);
                scb.add(or(SUPPORTS_ALL_CRITERION, matchesId(ProductionLineFields.GROUPS, id)));
            }
        };

        protected final SearchCriterion SUPPORTS_ALL_CRITERION = eq(ProductionLineFields.SUPPORTS_ALL_TECHNOLOGIES, true);

        protected SearchCriterion matchesId(final String fieldName, final Long id) {
            return eq(fieldName + DOT_ID, id);
        }

        public abstract void appendCriteria(final SearchCriteriaBuilder scb, final Long id);

    }
}
