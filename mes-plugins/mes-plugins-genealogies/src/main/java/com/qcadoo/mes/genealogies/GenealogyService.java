/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.genealogies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.internal.EntityTree;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.awesomeDynamicList.AwesomeDynamicListState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public final class GenealogyService {

    private static final String OPERATION_NODE_ENTITY_TYPE = "operation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/genealogies/orderGenealogies.html?context={\"order.id\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void newGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/genealogies/orderGenealogy.html?context={\"form.order\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void hideComponents(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        ComponentState featuresLayout = state.getComponentByReference("featuresLayout");
        ComponentState shiftList = state.getComponentByReference("shiftBorderLayout");
        FieldComponentState shiftFeaturesList = (FieldComponentState) state.getComponentByReference("shiftFeaturesList");
        ComponentState postList = state.getComponentByReference("postBorderLayout");
        FieldComponentState postFeaturesList = (FieldComponentState) state.getComponentByReference("postFeaturesList");
        ComponentState otherList = state.getComponentByReference("otherBorderLayout");
        FieldComponentState otherFeaturesList = (FieldComponentState) state.getComponentByReference("otherFeaturesList");

        Entity order = dataDefinitionService.get("products", "order").get(
                Long.valueOf(form.getEntity().getField("order").toString()));
        Entity technology = order.getBelongsToField("technology");

        if (technology != null) {
            boolean shiftFeatureRequired = (Boolean) technology.getField("shiftFeatureRequired");
            boolean postFeatureRequired = (Boolean) technology.getField("postFeatureRequired");
            boolean otherFeatureRequired = (Boolean) technology.getField("otherFeatureRequired");

            if (!shiftFeatureRequired) {
                shiftList.setVisible(false);
            } else {
                shiftFeaturesList.setRequired(true);
            }

            if (!postFeatureRequired) {
                postList.setVisible(false);
            } else {
                postFeaturesList.setRequired(true);
            }

            if (!otherFeatureRequired) {
                otherList.setVisible(false);
            } else {
                otherFeaturesList.setRequired(true);
            }

            if (!(otherFeatureRequired || shiftFeatureRequired || postFeatureRequired)) {
                featuresLayout.setVisible(false);
            }
        } else {
            featuresLayout.setVisible(false);
        }
    }

    public void fillProductInComponents(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        ComponentState productGridLayout = state.getComponentByReference("productGridLayout");
        AwesomeDynamicListState productInComponentsList = (AwesomeDynamicListState) state
                .getComponentByReference("productInComponentsList");

        if (form.isValid()) {
            Entity genealogy = null;
            List<Entity> existingProductInComponents = Collections.emptyList();

            if (form.getEntityId() != null) {
                genealogy = dataDefinitionService.get("genealogies", "genealogy").get(form.getEntityId());
                existingProductInComponents = genealogy.getHasManyField("productInComponents");
            }

            Entity order = dataDefinitionService.get("products", "order").get(
                    Long.valueOf(form.getEntity().getField("order").toString()));
            Entity technology = order.getBelongsToField("technology");

            if (technology != null) {
                List<Entity> targetProductInComponents = new ArrayList<Entity>();

                List<Entity> operationComponents = new ArrayList<Entity>();
                addOperationsFromSubtechnologiesToList(technology.getTreeField("operationComponents"), operationComponents);
                for (Entity operationComponent : operationComponents) {
                    for (Entity operationProductInComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                        if ((Boolean) operationProductInComponent.getField("batchRequired")) {
                            targetProductInComponents.add(createGenealogyProductInComponent(genealogy,
                                    operationProductInComponent, existingProductInComponents));
                        }
                    }
                }

                if (targetProductInComponents.isEmpty()) {
                    productGridLayout.setVisible(false);
                } else {
                    productInComponentsList.setFieldValue(targetProductInComponents);
                }
            } else {
                productGridLayout.setVisible(false);
            }
        }
    }

    private Entity createGenealogyProductInComponent(final Entity genealogy, final Entity operationProductInComponent,
            final List<Entity> existingProductInComponents) {
        for (Entity e : existingProductInComponents) {
            if (e.getBelongsToField("productInComponent").getId().equals(operationProductInComponent.getId())) {
                return e;
            }
        }
        Entity genealogyProductInComponent = dataDefinitionService.get("genealogies", "genealogyProductInComponent").create();
        genealogyProductInComponent.setField("genealogy", genealogy);
        genealogyProductInComponent.setField("productInComponent", operationProductInComponent);
        genealogyProductInComponent.setField("batch", new ArrayList<Entity>());
        return genealogyProductInComponent;
    }

    void addOperationsFromSubtechnologiesToList(final EntityTree entityTree, final List<Entity> operationComponents) {
        for (Entity operationComponent : entityTree) {
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
                operationComponents.add(operationComponent);
            } else {
                addOperationsFromSubtechnologiesToList(
                        operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents"),
                        operationComponents);
            }
        }
    }

}
