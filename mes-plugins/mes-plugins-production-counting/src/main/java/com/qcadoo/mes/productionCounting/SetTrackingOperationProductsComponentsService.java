package com.qcadoo.mes.productionCounting;

import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetTrackingOperationProductsComponentsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity fillTrackingOperationProductOutComponent(Entity productionTracking, Entity trackingOperationProductOutComponent, BigDecimal usedQuantity) {
        if (usedQuantity == null) {
            usedQuantity = BigDecimal.ZERO;
        }
        if (isSet(productionTracking, trackingOperationProductOutComponent)) {
            List<Entity> setTrackingOperationProductsInComponents = new ArrayList<>();
            DataDefinition setTrackingOperationProductInComponentsDD = getSetTrackingOperationProductInComponentsDD();

            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
            List<EntityTreeNode> children = operationComponents.getRoot().getChildren();

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
                    BigDecimal quantityFromSets = plannedQuantityFromProduct.multiply(usedQuantity).divide(plannedQuantityForOrder, RoundingMode.HALF_UP);
                    setTrackingOperationProductInComponents.setField("quantityFromSets", quantityFromSets);
                    setTrackingOperationProductInComponents.setField("product", productFromComponent);
                    setTrackingOperationProductInComponents.setField("trackingOperationProductOutComponent", trackingOperationProductOutComponent);

//                    setTrackingOperationProductInComponents = setTrackingOperationProductInComponents.getDataDefinition().save(setTrackingOperationProductInComponents);

                    setTrackingOperationProductsInComponents.add(setTrackingOperationProductInComponents);
                }
            }

            trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.SET_TRACKING_OPERATION_PRODUCTS_IN_COMPONENTS, setTrackingOperationProductsInComponents);
        }
        return trackingOperationProductOutComponent;
    }

    private DataDefinition getSetTrackingOperationProductInComponentsDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_SET_TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
    }

    public boolean isSet(Entity productionTracking, Entity componentEntity) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        boolean isSet = operationComponents.getRoot().getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).get(0).getBooleanField("set");

        return isSet;
    }
}
