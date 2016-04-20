package com.qcadoo.mes.basicProductionCounting;

import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantitySet;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductionCountingQuantitySetService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity fillSetField(final Entity productionCountingQuantity) {
        String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

        if (GlobalTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial) && ProductionCountingQuantityRole.USED.getStringValue().equals(role)) {
            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            if (product != null) {
                Entity technology = getTechnologyDD().find()
                        .add(SearchRestrictions.eq(TechnologyFields.MASTER, true))
                        .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product)).uniqueResult();
                if (technology != null) {
                    EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
                    Entity operationProductOutComponent = operationComponents.getRoot().getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).get(0);
                    boolean isSet = operationProductOutComponent.getBooleanField("set");
                    if (isSet) {
                        productionCountingQuantity.setField(ProductionCountingQuantityFields.SET, ProductionCountingQuantitySet.SET.getStringValue());
                    }
                }
            }

        } else if (GlobalTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial) && ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)) {
            Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

            Entity operationProductOutComponent = operationComponents.getRoot().getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).get(0);
            boolean isSet = operationProductOutComponent.getBooleanField("set");
            if (isSet) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.SET, ProductionCountingQuantitySet.SET.getStringValue());

                SearchCriteriaBuilder findProductionCountingQuantity = productionCountingQuantity.getDataDefinition().find();
                List<Entity> entities = findProductionCountingQuantity.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order)).list().getEntities();
                markIntermediateInProductionCountingQuantities(entities);
            }

        } else if (GlobalTypeOfMaterial.INTERMEDIATE.getStringValue().equals(typeOfMaterial) && ProductionCountingQuantityRole.USED.getStringValue().equals(role)) {
            Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
            SearchCriteriaBuilder findProductionCountingQuantity = productionCountingQuantity.getDataDefinition().find();
            List<Entity> entities = findProductionCountingQuantity.add(SearchRestrictions.and(
                    SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order),
                    SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL, GlobalTypeOfMaterial.FINAL_PRODUCT.getStringValue()),
                    SearchRestrictions.eq(ProductionCountingQuantityFields.SET, ProductionCountingQuantitySet.SET.getStringValue()))).list().getEntities();

            Entity technologyOperationComponent = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
            if (technologyOperationComponent != null) {
                Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                long count = entities.stream().filter(entity -> {
                    Entity entityTechnologyOperationComponent = entity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
                    Entity entityOperation = entityTechnologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                    return "1.".equals(entityTechnologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER))
                            && operation.getId().equals(entityOperation.getId());
                }).count();

                if (count > 0) {
                    productionCountingQuantity.setField(ProductionCountingQuantityFields.SET, ProductionCountingQuantitySet.INTERMEDIATE.getStringValue());
                }
            }
        }

        return productionCountingQuantity;
    }

    public void markIntermediateInProductionCountingQuantities(List<Entity> productionCountingQuantities) {
        for (Entity productionCountingQuantity : productionCountingQuantities) {
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
            String set = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.SET);

            if (GlobalTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial) && ProductionCountingQuantitySet.SET.getStringValue().equals(set)) {
                Entity technologyOperationComponent = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
                Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                for (Entity entity : productionCountingQuantities) {
                    Entity entityTechnologyOperationComponent = entity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
                    if (entityTechnologyOperationComponent == null) {
                        continue;
                    }
                    Entity entityOperation = entityTechnologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);
                    String entityTypeOfMaterial = entity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
                    String role = entity.getStringField(ProductionCountingQuantityFields.ROLE);

                    if (!entity.getId().equals(productionCountingQuantity.getId())
                            && ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                            && GlobalTypeOfMaterial.INTERMEDIATE.getStringValue().equals(entityTypeOfMaterial)
                            && "1.".equals(entityTechnologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER))
                            && operation.getId().equals(entityOperation.getId())) {
                        entity.setField(ProductionCountingQuantityFields.SET, ProductionCountingQuantitySet.INTERMEDIATE.getStringValue());
                        entity = entity.getDataDefinition().save(entity);
                    }
                }
            }
        }
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
    }

}
