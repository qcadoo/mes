/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.productionCounting.hooks;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.listeners.RecordOperationProductComponentListeners;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class RecordOperationProductInComponentHooks {

    @Autowired
    private RecordOperationProductComponentListeners recordOperationProductComponentListeners;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_FORM = "form";

    public void onBeforeRender(final ViewDefinitionState view) {
        recordOperationProductComponentListeners.onBeforeRender(view);
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity componentEntity = form.getPersistedEntityWithIncludedFormValues();

        if (hideOrShowSetTab(view, componentEntity)) {
            fillSetTab(view, componentEntity);
        }
    }

    private boolean hideOrShowSetTab(ViewDefinitionState view, Entity componentEntity) {
        boolean isSet = false;
        Entity product = componentEntity.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        DataDefinition operationProductOutComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
        SearchResult productOutComponents = operationProductOutComponentDD.find()
                .add(SearchRestrictions.eq(OperationProductOutComponentFields.PRODUCT + ".id", product.getId()))
                .add(SearchRestrictions.eq(OperationProductOutComponentFields.SET, true)).list();

        if (productOutComponents.getTotalNumberOfEntities() > 0) {
            isSet = productOutComponents.getEntities().stream()
                .map(poc -> poc.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT))
                .map(oc -> oc.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY))
                .anyMatch(t -> TechnologyState.ACCEPTED.getStringValue().equals(t.getStringField(TechnologyFields.STATE)));
        }

        if (!isSet) {
            view.getComponentByReference("setTab").setVisible(false);
        } else {
            view.getComponentByReference("setTab").setVisible(true);
        }

        return isSet;
    }

    private void fillSetTab(ViewDefinitionState view, Entity componentEntity) {

        List<Entity> setTechnologyInComponents = new ArrayList<>();
        DataDefinition setTechnologyInComponentsDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                "setTechnologyInComponents");

        Entity product = componentEntity.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        Entity technology = technologyDD.find()
                .add(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyState.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true))
                .add(SearchRestrictions.eq(OperationProductOutComponentFields.PRODUCT + ".id", product.getId())).uniqueResult();

        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        List<EntityTreeNode> firstLevelComponents = operationComponents.getRoot().getChildren();
        for (EntityTreeNode firstLevelComponent : firstLevelComponents) {
            EntityList operationProductInComponents = firstLevelComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
            if (operationProductInComponents != null) {
                Entity setTechnologyInComponent = setTechnologyInComponentsDD.create();

                BigDecimal quantityFromSets = operationComponents.get(0).getDecimalField(
                        OperationProductInComponentFields.GIVEN_QUANTITY);
                setTechnologyInComponent.setField("quantityFromSets", quantityFromSets);
                setTechnologyInComponent.setField("operationProductInComponent", operationProductInComponents.get(0));
                setTechnologyInComponent.setField("trackingOperationProductOutComponent", componentEntity);

                setTechnologyInComponents.add(setTechnologyInComponent);
            }
        }

        componentEntity.setField("setTechnologyInComponents", setTechnologyInComponents);
        componentEntity.getDataDefinition().save(componentEntity);
    }
}
