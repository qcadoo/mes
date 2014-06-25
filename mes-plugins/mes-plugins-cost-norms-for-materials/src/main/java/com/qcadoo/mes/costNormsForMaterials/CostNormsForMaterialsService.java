/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.costNormsForMaterials;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class CostNormsForMaterialsService {

    private static final Logger LOG = LoggerFactory.getLogger(CostNormsForMaterialsService.class);

    private static final String L_GRID = "grid";

    private static final String L_FORM = "form";

    private static final String L_ORDER = "order";

    private static final String L_VIEW_DEFINITION_STATE_IS_NULL = "viewDefinitionState is null";

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void fillInProductsGridInTechnology(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, L_VIEW_DEFINITION_STATE_IS_NULL);

        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(L_GRID);
        FormComponent technology = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);

        Long technologyId = technology.getEntityId();

        if (technologyId == null) {
            return;
        }

        List<Entity> inputProducts = Lists.newArrayList();

        Map<Long, BigDecimal> productQuantities = getProductQuantitiesFromTechnology(technologyId);

        if (!productQuantities.isEmpty()) {
            for (Map.Entry<Long, BigDecimal> productQuantity : productQuantities.entrySet()) {
                Entity product = productQuantitiesService.getProduct(productQuantity.getKey());
                BigDecimal quantity = productQuantity.getValue();

                Entity operationProductInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();

                operationProductInComponent.setField(OperationProductInComponentFields.PRODUCT, product);
                operationProductInComponent.setField(OperationProductInComponentFields.QUANTITY, quantity);

                inputProducts.add(operationProductInComponent);
            }
        }

        grid.setEntities(inputProducts);
    }

    public Map<Long, BigDecimal> getProductQuantitiesFromTechnology(final Long technologyId) {
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        Entity operationComponentRoot = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

        if (operationComponentRoot != null) {
            try {
                BigDecimal giventQty = technologyService.getProductCountForOperationComponent(operationComponentRoot);

                Map<Long, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                        giventQty, MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS);

                return productQuantities;
            } catch (IllegalStateException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Invalid technology tree!");
                }
            }
        }

        return Maps.newHashMap();
    }

    public void copyCostsFromProducts(final ViewDefinitionState viewDefinitionState, final ComponentState component,
            final String[] args) {
        checkArgument(viewDefinitionState != null, L_VIEW_DEFINITION_STATE_IS_NULL);

        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(L_GRID);

        FormComponent order = (FormComponent) viewDefinitionState.getComponentByReference(L_ORDER);

        if ((order == null) || (order.getEntityId() == null)) {
            return;
        }

        Long orderId = order.getEntityId();

        List<Entity> inputProducts = Lists.newArrayList();

        Entity existingOrder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                orderId);

        List<Entity> technologyInstOperProductInComps = existingOrder
                .getHasManyField(CostNormsForMaterialsConstants.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS);

        if (technologyInstOperProductInComps != null) {
            for (Entity technologyInstOperProductInComp : technologyInstOperProductInComps) {
                Entity product = technologyInstOperProductInComp.getBelongsToField(TechnologyInstOperProductInCompFields.PRODUCT);

                technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.COST_FOR_NUMBER,
                        product.getField(ProductFieldsCNFP.COST_FOR_NUMBER));
                technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.NOMINAL_COST,
                        product.getField(ProductFieldsCNFP.NOMINAL_COST));
                technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST,
                        product.getField(ProductFieldsCNFP.LAST_PURCHASE_COST));
                technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.AVERAGE_COST,
                        product.getField(ProductFieldsCNFP.AVERAGE_COST));

                technologyInstOperProductInComp = technologyInstOperProductInComp.getDataDefinition().save(
                        technologyInstOperProductInComp);

                inputProducts.add(technologyInstOperProductInComp);
            }
        }

        grid.setEntities(inputProducts);
    }

    public void updateCostsForProductInOrder(Entity order, Long productId, Optional<BigDecimal> costForNumber,
                                              Optional<BigDecimal> costForOrder) {
        Entity technologyInstOperProductInComp = getTechnologyInstOperProductInCompFromDB(productId, order);
        Preconditions.checkArgument(technologyInstOperProductInComp != null, String.format(
                "TechnologyInstanceOperationProductInComponent not found for product: %d order: %d", productId, order.getId()));

        technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.COST_FOR_ORDER,
                numberService.setScale(costForOrder.or(BigDecimal.ZERO)));

        Optional<BigDecimal> oldQuantity = Optional.fromNullable(technologyInstOperProductInComp
                .getDecimalField(TechnologyInstOperProductInCompFields.COST_FOR_NUMBER));

        if (oldQuantity.isPresent()) {
            if (BigDecimal.ZERO.equals(oldQuantity.get())) {
                oldQuantity = Optional.of(BigDecimal.ONE);
            }

            BigDecimal nominalCost = technologyInstOperProductInComp
                    .getDecimalField(TechnologyInstOperProductInCompFields.NOMINAL_COST);
            BigDecimal lastPurchaseCost = technologyInstOperProductInComp
                    .getDecimalField(TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST);
            BigDecimal averageCost = technologyInstOperProductInComp
                    .getDecimalField(TechnologyInstOperProductInCompFields.AVERAGE_COST);

            nominalCost = costForNumber.or(BigDecimal.ONE).multiply(nominalCost, numberService.getMathContext())
                    .divide(oldQuantity.get(), numberService.getMathContext());
            lastPurchaseCost = costForNumber.or(BigDecimal.ONE).multiply(lastPurchaseCost, numberService.getMathContext())
                    .divide(oldQuantity.get(), numberService.getMathContext());
            averageCost = costForNumber.or(BigDecimal.ONE).multiply(averageCost, numberService.getMathContext())
                    .divide(oldQuantity.get(), numberService.getMathContext());

            technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.COST_FOR_NUMBER,
                    numberService.setScale(costForNumber.or(BigDecimal.ONE)));
            technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.NOMINAL_COST,
                    numberService.setScale(nominalCost));
            technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST,
                    numberService.setScale(lastPurchaseCost));
            technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.AVERAGE_COST,
                    numberService.setScale(averageCost));

        } else {
            LOG.debug(String.format(
                    "There are no costs in TechnologyInstanceOperationProductInComponent (id: %d ) to recalculate.",
                    technologyInstOperProductInComp.getId()));
        }

        technologyInstOperProductInComp.getDataDefinition().save(technologyInstOperProductInComp);
    }

    private Entity getTechnologyInstOperProductInCompFromDB(final Long productId, final Entity orderFromDB) {
        return dataDefinitionService
                .get(CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                        CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP)
                .find()
                .add(SearchRestrictions.belongsTo(TechnologyInstOperProductInCompFields.PRODUCT,
                        BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT, productId))
                .add(SearchRestrictions.belongsTo(TechnologyInstOperProductInCompFields.ORDER, orderFromDB)).setMaxResults(1)
                .uniqueResult();
    }
}
