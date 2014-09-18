package com.qcadoo.mes.technologies.tree.dataProvider;

import static com.qcadoo.model.api.search.SearchRestrictions.idEq;
import static com.qcadoo.model.api.search.SearchRestrictions.isEmpty;
import static com.qcadoo.model.api.search.SearchRestrictions.neField;
import static com.qcadoo.model.api.search.SearchRestrictions.or;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchProjection;

@Service
final class TechnologyRawInputProductComponentsDataProviderImpl implements TechnologyRawInputProductComponentsDataProvider {

    private static final String TOC_ALIAS = "toc_alias";

    private static final String TOC_CHILDREN_ALIAS = "tocChildren_alias";

    private static final String OPOC_ALIAS = "opoc_alias";

    private static final String OPIC_ALIAS = "opic_alias";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<Entity> findAll(final TechnologyRawInputProductComponentsCriteria criteria) {
        SearchCriteriaBuilder scb = createCriteriaBuilder();
        applyDefaultCriteria(criteria, scb);
        applyProjection(criteria, scb);
        applyProductCriteria(criteria, scb);
        applySearchOrder(criteria, scb);
        return scb.list().getEntities();
    }

    private void applyDefaultCriteria(final TechnologyRawInputProductComponentsCriteria criteria, final SearchCriteriaBuilder scb) {
        scb.add(or(
                neField(OPOC_ALIAS + "." + OperationProductOutComponentFields.PRODUCT, OPIC_ALIAS + "."
                        + OperationProductInComponentFields.PRODUCT), isEmpty(TOC_ALIAS + "."
                        + TechnologyOperationComponentFields.CHILDREN)));
        scb.createCriteria(TOC_ALIAS + "." + TechnologyOperationComponentFields.TECHNOLOGY, "tech_alias", JoinType.INNER).add(
                idEq(criteria.getTechnologyId()));
    }

    private void applyProductCriteria(final TechnologyRawInputProductComponentsCriteria criteria, final SearchCriteriaBuilder scb) {
        for (SearchCriterion searchCriterion : criteria.getProductCriteria().asSet()) {
            scb.createCriteria(OperationProductInComponentFields.PRODUCT, "prod_alias", JoinType.INNER).add(searchCriterion);
        }
    }

    private void applySearchOrder(final TechnologyRawInputProductComponentsCriteria criteria, final SearchCriteriaBuilder scb) {
        for (SearchOrder searchOrder : criteria.getSearchOrder().asSet()) {
            scb.addOrder(searchOrder);
        }
    }

    private void applyProjection(final TechnologyRawInputProductComponentsCriteria criteria, final SearchCriteriaBuilder scb) {
        for (SearchProjection searchProjection : criteria.getSearchProjection().asSet()) {
            scb.setProjection(searchProjection);
        }
    }

    private SearchCriteriaBuilder createCriteriaBuilder() {
        SearchCriteriaBuilder scb = getOpicDataDefinition().findWithAlias(OPIC_ALIAS);
        scb.createAlias(OperationProductInComponentFields.OPERATION_COMPONENT, TOC_ALIAS, JoinType.INNER);
        scb.createAlias(OperationProductInComponentFields.PRODUCT, TechnologyRawInputProductComponentsCriteria.PRODUCT_ALIAS,
                JoinType.INNER);
        scb.createAlias(TOC_ALIAS + "." + TechnologyOperationComponentFields.CHILDREN, TOC_CHILDREN_ALIAS, JoinType.LEFT);
        scb.createAlias(TOC_CHILDREN_ALIAS + "." + TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS,
                OPOC_ALIAS, JoinType.LEFT);
        return scb;
    }

    private DataDefinition getOpicDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }
}
