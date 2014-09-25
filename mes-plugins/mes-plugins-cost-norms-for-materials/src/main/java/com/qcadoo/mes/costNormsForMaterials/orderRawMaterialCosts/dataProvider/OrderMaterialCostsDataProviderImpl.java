package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchProjection;

@Service
final class OrderMaterialCostsDataProviderImpl implements OrderMaterialCostsDataProvider {

    private static final String ORDER_MATERIAL_COST_ALIAS = "orderMaterialCosts_alias";

    private static final String ORDER_ALIAS = "order_alias";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<Entity> findAll(final OrderMaterialCostsCriteria criteria) {
        return prepareCriteria(criteria).list().getEntities();
    }

    @Override
    public Optional<Entity> find(final OrderMaterialCostsCriteria criteria) {
        return Optional.fromNullable(prepareCriteria(criteria).setMaxResults(1).uniqueResult());
    }

    @Override
    public Optional<Entity> find(final Long orderId, final Long productId) {
        return find(OrderMaterialCostsCriteria.forOrder(orderId).setProductCriteria(idEq(productId)));
    }

    private SearchCriteriaBuilder prepareCriteria(final OrderMaterialCostsCriteria criteria) {
        SearchCriteriaBuilder scb = createCriteriaBuilder();
        scb.createCriteria(TechnologyInstOperProductInCompFields.ORDER, ORDER_ALIAS, JoinType.INNER).add(
                idEq(criteria.getOrderId()));
        applyProjection(criteria, scb);
        applyProductCriteria(criteria, scb);
        applySearchOrder(criteria, scb);
        return scb;
    }

    private void applyProjection(final OrderMaterialCostsCriteria criteria, final SearchCriteriaBuilder scb) {
        for (SearchProjection searchProjection : criteria.getSearchProjection().asSet()) {
            scb.setProjection(searchProjection);
        }
    }

    private void applyProductCriteria(final OrderMaterialCostsCriteria criteria, final SearchCriteriaBuilder scb) {
        for (SearchCriterion searchCriterion : criteria.getProductCriteria().asSet()) {
            scb.createCriteria(TechnologyInstOperProductInCompFields.PRODUCT, OrderMaterialCostsCriteria.PRODUCT_ALIAS,
                    JoinType.INNER).add(searchCriterion);
        }
    }

    private void applySearchOrder(final OrderMaterialCostsCriteria criteria, final SearchCriteriaBuilder scb) {
        for (SearchOrder searchOrder : criteria.getSearchOrder().asSet()) {
            scb.addOrder(searchOrder);
        }
    }

    private SearchCriteriaBuilder createCriteriaBuilder() {
        return getOrderMaterialCostsDataDef().findWithAlias(ORDER_MATERIAL_COST_ALIAS);
    }

    private DataDefinition getOrderMaterialCostsDataDef() {
        return dataDefinitionService.get(CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP);
    }
}
