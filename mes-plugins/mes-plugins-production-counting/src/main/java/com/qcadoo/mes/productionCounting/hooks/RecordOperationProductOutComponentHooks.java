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

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.listeners.RecordOperationProductComponentListeners;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecordOperationProductOutComponentHooks {

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
        boolean isSet = true;
//        Entity productionTracking = componentEntity
//                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
//        Entity product = componentEntity.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
//
//        Entity technologyOperationComponent = productionTracking
//                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
//
//        EntityList operationProductOutComponents = technologyOperationComponent
//                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);
//
//        Optional<Entity> operationProductOutComponent = operationProductOutComponents
//                .stream()
//                .filter(opoc -> opoc.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId()
//                        .equals(product.getId())).findFirst();
//        if (operationProductOutComponent.isPresent()) {
//            isSet = operationProductOutComponent.get().getBooleanField("set");
//        }
        if (!isSet) {
            view.getComponentByReference("setTab").setVisible(false);
        } else {
            view.getComponentByReference("setTab").setVisible(true);
        }

        return isSet;
    }

    private void fillSetTab(ViewDefinitionState view, Entity componentEntity) {
        List<Entity> setTrackingOperationProductsInComponents = new ArrayList<>();
        DataDefinition setTrackingOperationProductInComponentsDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_SET_TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        Entity productionTracking = componentEntity.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        
        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        List<EntityTreeNode> children = operationComponents.getRoot().getChildren();

        FieldComponent usedQuantityField = (FieldComponent) view.getComponentByReference("usedQuantity");

        BigDecimal usedQuantity = BigDecimal.ZERO;
        String usedQuantityString = usedQuantityField.getFieldValue().toString().replace(",", ".").replace(" ", "");
        if (!Strings.isNullOrEmpty(usedQuantityString)) {
            usedQuantity = new BigDecimal(usedQuantityString);
        }

        for (Entity technologyOperationComponent : children) {
            Entity operationProductOutComponent = technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).get(0);
            
            Entity productFromComponent = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);
            GlobalTypeOfMaterial productGlobalTypeOfMaterial = GlobalTypeOfMaterial.parseString(productFromComponent.getStringField(ProductFields.GLOBAL_TYPE_OF_MATERIAL));

            if (productGlobalTypeOfMaterial == GlobalTypeOfMaterial.INTERMEDIATE) {
                Entity setTrackingOperationProductInComponents = setTrackingOperationProductInComponentsDD.create();

                DataDefinition productionCountingQuantityDD = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
                Entity productionCountingQuantity = productionCountingQuantityDD.find()
                        .add(SearchRestrictions.and(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, productFromComponent),
                                SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order),
                                SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.USED.getStringValue()))).uniqueResult();
                BigDecimal plannedQuantityFromProduct = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
                
                BigDecimal plannedQuantityForOrder = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
                BigDecimal quantityFromSets = plannedQuantityFromProduct.divide(plannedQuantityForOrder, RoundingMode.HALF_UP).multiply(usedQuantity);
                setTrackingOperationProductInComponents.setField("quantityFromSets", quantityFromSets);
                setTrackingOperationProductInComponents.setField("product", productFromComponent);
                setTrackingOperationProductInComponents.setField("trackingOperationProductOutComponent", componentEntity);
                
                setTrackingOperationProductInComponents = setTrackingOperationProductInComponents.getDataDefinition().save(setTrackingOperationProductInComponents);
                
                setTrackingOperationProductsInComponents.add(setTrackingOperationProductInComponents);
            }
        }

        componentEntity.setField(TrackingOperationProductOutComponentFields.SET_TRACKING_OPERATION_PRODUCTS_IN_COMPONENTS, setTrackingOperationProductsInComponents);
        componentEntity.getDataDefinition().save(componentEntity);
        GridComponent gridComponent = (GridComponent) view.getComponentByReference("setTrackingOperationProductsInComponents");
        gridComponent.setEntities(setTrackingOperationProductsInComponents);
    }
}
