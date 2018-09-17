/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.ordersForSubproductsGeneration.listeners;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.MaterialRequirementCoverageForOrderService;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageForOrderFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageService;
import com.qcadoo.mes.orderSupplies.register.RegisterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.ordersForSubproductsGeneration.OrdersForSubproductsGenerationService;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.CoverageForOrderFieldsOFSPG;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrderFieldsOFSPG;
import com.qcadoo.mes.technologies.constants.ProductStructureTreeNodeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrdersForSubproductsGenerationListeners {

    protected static final Logger LOG = LoggerFactory.getLogger(OrdersForSubproductsGenerationListeners.class);

    private static final String L_FORM = "form";

    @Autowired
    private OrdersForSubproductsGenerationService ordersForSubproductsGenerationService;

    @Autowired
    private MaterialRequirementCoverageService materialRequirementCoverageService;

    @Autowired
    private MaterialRequirementCoverageForOrderService forOrderService;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void generateSimpleOrdersForSubProducts(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity entity = form.getEntity();
        Entity entityDB = entity.getDataDefinition().get(entity.getId());
        try {
            fillGenerationProgressFlag(entityDB, true);
            simpleGenerate(view, state, args);
        } catch (Exception ex) {
            LOG.error("Error when generation orders for components : ", ex);
            view.addMessage("qcadooView.errorPage.error.internalError.explanation", ComponentState.MessageType.FAILURE);
        } finally {
            fillGenerationProgressFlag(entityDB, false);
        }

    }

    public final void generateOrdersForSubProducts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Optional<Entity> optionalMrc = getGenerateingMRC();
        if (optionalMrc.isPresent()) {
            Entity mrc = getGenerateingMRC().get();
            if (mrc.getBelongsToField(CoverageForOrderFields.ORDER) == null) {
                state.addMessage("ordersForSubproductsGeneration.generationSubOrdersAction.generationInProgressSimple",
                        ComponentState.MessageType.INFO, false);
            } else {
                state.addMessage("ordersForSubproductsGeneration.generationSubOrdersAction.generationInProgress",
                        ComponentState.MessageType.INFO, false, mrc.getBelongsToField(CoverageForOrderFields.ORDER)
                                .getStringField(OrderFields.NUMBER));
            }

        } else {
            try {
                if (hasAlreadyGeneratedOrders(view)) {
                    state.addMessage("ordersForSubproductsGeneration.generationSubOrdersAction.ordersAlreadyGenerated",
                            ComponentState.MessageType.INFO, false);
                    return;
                }
                fillGenerationProgressFlag(view, state, true);
                generate(view, state, args);
            } catch (Exception ex) {
                LOG.error("Error when generation orders for components : ", ex);
                view.addMessage("qcadooView.errorPage.error.internalError.explanation", ComponentState.MessageType.FAILURE);
            } finally {
                fillGenerationProgressFlag(view, state, false);
            }

        }
    }

    private boolean hasAlreadyGeneratedOrders(final ViewDefinitionState view) {
        FormComponent coverageForm = (FormComponent) view.getComponentByReference(L_FORM);
        DataDefinition dataDefinition = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
        Long coverageId = coverageForm.getEntityId();

        Entity coverageEntity = coverageForm.getPersistedEntityWithIncludedFormValues();
        Entity order = coverageEntity.getBelongsToField(CoverageForOrderFields.ORDER);
        List<Entity> coverageOrders = coverageEntity.getHasManyField(MaterialRequirementCoverageFields.COVERAGE_ORDERS);
        if (order != null) {
            List<Entity> entities = dataDefinition
                    .find()
                    .add(SearchRestrictions.belongsTo(OrderFieldsOFSPG.ROOT, OrdersConstants.PLUGIN_IDENTIFIER,
                            OrdersConstants.MODEL_ORDER, order.getId()))
                    .add(SearchRestrictions.isNotNull(OrderFieldsOFSPG.PARENT)).list().getEntities();
            if (!entities.isEmpty()) {
                return true;
            }
        } else if (!coverageOrders.isEmpty()) {
            List<Long> ids = coverageOrders.stream().map(co -> co.getId()).collect(Collectors.toList());
            List<Entity> entities = dataDefinition.find()
                    .createAlias(OrderFieldsOFSPG.ROOT, OrderFieldsOFSPG.ROOT, JoinType.LEFT)
                    .add(SearchRestrictions.in(OrderFieldsOFSPG.ROOT + ".id", Lists.newArrayList(ids)))
                    .add(SearchRestrictions.isNotNull(OrderFieldsOFSPG.PARENT)).list().getEntities();
            if (!entities.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void fillGenerationProgressFlag(final ViewDefinitionState view, final ComponentState state, final boolean option) {
        FormComponent materialRequirementForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity materialRequirementEntity = materialRequirementForm.getEntity().getDataDefinition()
                .get(materialRequirementForm.getEntity().getId());
        materialRequirementEntity.setField("orderGenerationInProgress", option);
        materialRequirementEntity.getDataDefinition().fastSave(materialRequirementEntity);
    }

    private void fillGenerationProgressFlag(Entity subOrders, final boolean option) {
        subOrders.setField("orderGenerationInProgress", option);
        subOrders.getDataDefinition().fastSave(subOrders);
    }

    private Optional<Entity> getGenerateingMRC() {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE).find();
        searchCriteriaBuilder.add(SearchRestrictions.eq("orderGenerationInProgress", true));
        List<Entity> entities = searchCriteriaBuilder.list().getEntities();
        if (entities.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(entities.get(0));
        }
    }

    private void showMessagesForNotAcceptedComponents(final ViewDefinitionState view, final Entity order) {
        List<Entity> nodes = ordersForSubproductsGenerationService.getProductNodesWithCheckedTechnologies(view, order);
        if (!nodes.isEmpty()) {
            String componentsWithCheckedTechnology = nodes
                    .stream()
                    .map(node -> node.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT).getStringField(
                            ProductFields.NUMBER)).collect(Collectors.joining(", "));
            view.addMessage("ordersForSubproductsGeneration.ordersForSubproducts.generate.componentsWithCheckedTechnologies",
                    ComponentState.MessageType.INFO, false, componentsWithCheckedTechnology);
        }
    }

    private final void generate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent materialRequirementForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long materialRequirementCoverageId = materialRequirementForm.getEntityId();
        LOG.info(String.format("Start generation orders for components. Material requirement coverage : %d",
                materialRequirementCoverageId));

        Integer generatedOrders = 0;

        if (materialRequirementCoverageId != null) {
            Entity materialRequirementEntity = materialRequirementForm.getEntity();
            Entity materialRequirementEntityDB = materialRequirementEntity.getDataDefinition().get(
                    materialRequirementEntity.getId());
            List<Entity> orders = materialRequirementEntityDB.getHasManyField(MaterialRequirementCoverageFields.COVERAGE_ORDERS);
            for (Entity orderEntity : orders) {
                showMessagesForNotAcceptedComponents(view, orderEntity);
                List<Entity> products = ordersForSubproductsGenerationService.getComponentProducts(materialRequirementEntity,
                        orderEntity);
                int index = 1;
                for (Entity coverageProduct : products) {
                    ordersForSubproductsGenerationService.generateOrderForSubProduct(coverageProduct, orderEntity,
                            state.getLocale(), index);
                    ++index;
                    ++generatedOrders;
                }
                if (!products.isEmpty()) {
                    materialRequirementEntity.setField(CoverageForOrderFieldsOFSPG.GENERATED_ORDERS, true);
                    materialRequirementEntity.getDataDefinition().save(materialRequirementEntity);
                }
                index = 1;
                boolean generateSubOrdersForTree = true;

                List<Entity> subOrdersForActualLevel = Lists.newArrayList();

                while (generateSubOrdersForTree) {

                    subOrdersForActualLevel = ordersForSubproductsGenerationService.getSubOrdersForRootAndLevel(orderEntity,
                            index);

                    if (subOrdersForActualLevel.isEmpty()) {
                        generateSubOrdersForTree = false;
                    }
                    for (Entity sorder : subOrdersForActualLevel) {
                        Optional<Entity> oCoverage = forOrderService.createMRCFO(sorder, materialRequirementEntity);

                        if (oCoverage.isPresent()) {
                            materialRequirementCoverageService.estimateProductCoverageInTime(oCoverage.get());

                            List<Entity> productss = ordersForSubproductsGenerationService.getCoverageProductsForOrder(
                                    oCoverage.get(), sorder);

                            int in = 1;
                            for (Entity coverageProduct : productss) {
                                ordersForSubproductsGenerationService.generateOrderForSubProduct(coverageProduct, sorder,
                                        state.getLocale(), in);
                                ++in;
                                ++generatedOrders;
                            }

                        } else {
                            state.addMessage("ordersForSubproductsGeneration.generationSubOrdersAction.coverageErrors",
                                    ComponentState.MessageType.FAILURE, false);
                            return;
                        }

                    }

                    ++index;

                }

            }
        }
        if (generatedOrders > 0) {
            state.addMessage("ordersForSubproductsGeneration.generationSubOrdersAction.generatedMessageSuccess",
                    ComponentState.MessageType.SUCCESS, false, generatedOrders.toString());
        } else {
            state.addMessage("ordersForSubproductsGeneration.generationSubOrdersAction.generatedMessageSuccessNoOrders",
                    ComponentState.MessageType.SUCCESS, false);
        }
        LOG.info(String.format("Finish generation orders for components. Material requirement coverage : %d",
                materialRequirementCoverageId));
    }

    private final void simpleGenerate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        LOG.info(String.format("Start generation orders for components"));

        Integer generatedOrders = 0;

        Entity entity = form.getEntity();
        Entity entityDB = entity.getDataDefinition().get(entity.getId());
        List<Entity> orders = Lists.newArrayList();

        if (Objects.nonNull(entityDB.getBelongsToField("ordersGroup"))) {
            orders = entityDB.getBelongsToField("ordersGroup").getHasManyField("orders");
        } else if (Objects.nonNull(entityDB.getBelongsToField("order"))) {
            orders.add(entityDB.getBelongsToField("order"));
        }
        for (Entity orderEntity : orders) {
            showMessagesForNotAcceptedComponents(view, orderEntity);
            List<Entity> registryEntries = registerService.findComponentRegistryEntries(orderEntity);

            int index = 1;
            for (Entity registryEntry : registryEntries) {
                ordersForSubproductsGenerationService.generateSimpleOrderForSubProduct(registryEntry, orderEntity,
                        state.getLocale(), index);
                ++index;
                ++generatedOrders;
            }
            if (!registryEntries.isEmpty()) {
                entity.setField("generatedOrders", true);
                entity.getDataDefinition().save(entity);
            }
            index = 1;
            boolean generateSubOrdersForTree = true;

            List<Entity> subOrdersForActualLevel = Lists.newArrayList();

            while (generateSubOrdersForTree) {

                subOrdersForActualLevel = ordersForSubproductsGenerationService.getSubOrdersForRootAndLevel(orderEntity, index);

                if (subOrdersForActualLevel.isEmpty()) {
                    generateSubOrdersForTree = false;
                }
                for (Entity sorder : subOrdersForActualLevel) {

                    List<Entity> entries = registerService.findComponentRegistryEntries(sorder);

                    int in = 1;
                    for (Entity _entry : entries) {
                        ordersForSubproductsGenerationService.generateSimpleOrderForSubProduct(_entry, sorder, state.getLocale(),
                                in);
                        ++in;
                        ++generatedOrders;
                    }

                }

                ++index;

            }

        }

        if (generatedOrders > 0) {
            state.addMessage("ordersForSubproductsGeneration.generationSubOrdersAction.generatedMessageSuccess",
                    ComponentState.MessageType.SUCCESS, false, generatedOrders.toString());
        } else {
            state.addMessage("ordersForSubproductsGeneration.generationSubOrdersAction.generatedMessageSuccessNoOrders",
                    ComponentState.MessageType.SUCCESS, false);
        }
        LOG.info(String.format("Finish generation orders for components."));
    }
}
