package com.qcadoo.mes.costCalculation.print;

import com.google.common.collect.Lists;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.print.utils.CostCalculationMaterial;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CostCalculationComponentsService {

    private static final String L_COMPONENT = "component";

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private NumberService numberService;

    public void addMaterialOperationCost(final Entity costCalculation,
            final Map<Long, ComponentsCalculationHolder> basicComponentsMap,
            final Map<OperationProductComponentHolder, BigDecimal> materialQuantitiesByOPC) {
        MathContext mathContext = numberService.getMathContext();
        List<CostCalculationMaterial> list = Lists.newArrayList();
        Entity order = costCalculation.getBelongsToField(CostCalculationFields.ORDER);
        for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : materialQuantitiesByOPC.entrySet()) {
            Entity product = neededProductQuantity.getKey().getProduct();

            Entity productEntity = productsCostCalculationService.getAppropriateCostNormForProduct(product, order,
                    costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS));

            BigDecimal productQuantity = neededProductQuantity.getValue();

            BigDecimal costForGivenQuantity = productsCostCalculationService.calculateProductCostForGivenQuantity(productEntity,
                    productQuantity, costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE));

            if (basicComponentsMap.containsKey((neededProductQuantity.getKey().getTechnologyOperationComponentId()))) {
                ComponentsCalculationHolder cc = basicComponentsMap.get(neededProductQuantity.getKey()
                        .getTechnologyOperationComponentId());
                BigDecimal materialCost = BigDecimalUtils.convertNullToZero(cc.getMaterialCost()).add(
                        BigDecimalUtils.convertNullToZero(costForGivenQuantity), mathContext);
                cc.setMaterialCost(numberService.setScale(materialCost, 2));
            }

        }
    }

    public void addOperationCost(final Entity costCalculation, final Map<Long, ComponentsCalculationHolder> components) {
        MathContext mathContext = numberService.getMathContext();

        List<Entity> calculationOperationComponents = costCalculation
                .getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS);

        for (Entity calculationOperationComponent : calculationOperationComponents) {
            BigDecimal totalMachineOperationCost = calculationOperationComponent
                    .getDecimalField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST);
            BigDecimal totalLaborOperationCost = calculationOperationComponent
                    .getDecimalField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST);
            if (components.containsKey(calculationOperationComponent.getBelongsToField("technologyOperationComponent").getId())) {
                ComponentsCalculationHolder holder = components.get(calculationOperationComponent.getBelongsToField(
                        "technologyOperationComponent").getId());
                BigDecimal cost = BigDecimalUtils.convertNullToZero(totalMachineOperationCost).add(
                        BigDecimalUtils.convertNullToZero(totalLaborOperationCost), mathContext);
                holder.setLaborCost(numberService.setScale(cost, 2));
            }
        }

    }

    public List<ComponentsCalculationHolder> fillAllOperations(final EntityTree operationComponents) {
        List<ComponentsCalculationHolder> components = Lists.newArrayList();
        for (Entity toc : operationComponents) {
            ComponentsCalculationHolder component = new ComponentsCalculationHolder(toc,
                    toc.getBelongsToField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE));
            components.add(component);
        }
        return components;
    }

    public List<ComponentsCalculationHolder> fillBasicComponents(final EntityTree operationComponents) {
        List<ComponentsCalculationHolder> basicComponents = Lists.newArrayList();
        Map<Long, Entity> componentsById = operationComponents.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));
        List<Entity> tocs = operationComponents.stream()
                .filter(pc -> L_COMPONENT.equals(pc.getStringField(TechnologyOperationComponentFields.TYPE_FROM_STRUCTURE_TREE)))
                .collect(Collectors.toList());
        for (Entity toc : tocs) {
            if (toc.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).stream()
                    .filter(op -> op.getBooleanField(OperationProductOutComponentFields.SET)).findAny().isPresent()) {
                List<Entity> setOperations = toc.getHasManyField(TechnologyOperationComponentFields.CHILDREN);
                for (Entity setOperation : setOperations) {
                    Entity op = componentsById.get(setOperation.getId());
                    if (!L_COMPONENT.equals(op.getStringField(TechnologyOperationComponentFields.TYPE_FROM_STRUCTURE_TREE))) {
                        ComponentsCalculationHolder component = new ComponentsCalculationHolder(op,
                                op.getBelongsToField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE));
                        basicComponents.add(component);
                    }
                }
            } else {
                ComponentsCalculationHolder component = new ComponentsCalculationHolder(toc,
                        toc.getBelongsToField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE));
                basicComponents.add(component);
            }

        }
        return basicComponents;
    }

    public void fillBasicComponentsCosts(final EntityTree operationComponents,
            final Map<Long, ComponentsCalculationHolder> basicComponentsMap,
            final Map<Long, ComponentsCalculationHolder> allOperationsMap, final BigDecimal quantity) {
        Map<Long, Entity> entitiesById = new LinkedHashMap<Long, Entity>();
        MathContext mathContext = numberService.getMathContext();

        for (Entity entity : operationComponents) {
            entitiesById.put(entity.getId(), entity);
        }

        for (Map.Entry<Long, ComponentsCalculationHolder> entry : basicComponentsMap.entrySet()) {
            ComponentsCalculationHolder component = entry.getValue();
            component.setMaterialCost(allOperationsMap.get(entry.getKey()).getMaterialCost());
            component.setLaborCost(allOperationsMap.get(entry.getKey()).getLaborCost());
            addChildCost(component, null, allOperationsMap, entitiesById, mathContext);
            BigDecimal sumOfCost = BigDecimalUtils.convertNullToZero(component.getLaborCost()).add(
                    BigDecimalUtils.convertNullToZero(component.getMaterialCost()), mathContext);
            BigDecimal costPerUnit = sumOfCost.divide(quantity, mathContext);
            component.setSumOfCost(numberService.setScale(sumOfCost, 2));
            component.setCostPerUnit(numberService.setScale(costPerUnit, 2));
        }

    }

    private void addChildCost(ComponentsCalculationHolder component, ComponentsCalculationHolder child,
            Map<Long, ComponentsCalculationHolder> allOperationsMap, Map<Long, Entity> entitiesById, MathContext mathContext) {
        ComponentsCalculationHolder _component = component;

        if (child != null) {
            BigDecimal materialCost = BigDecimalUtils.convertNullToZero(component.getMaterialCost()).add(
                    BigDecimalUtils.convertNullToZero(child.getMaterialCost()), mathContext);
            BigDecimal laborCost = BigDecimalUtils.convertNullToZero(component.getLaborCost()).add(
                    BigDecimalUtils.convertNullToZero(child.getLaborCost()), mathContext);
            component.setLaborCost(numberService.setScale(laborCost, 2));
            component.setMaterialCost(numberService.setScale(materialCost, 2));
            _component = child;
        }

        for (Entity toc : entitiesById.get(_component.getToc().getId()).getHasManyField(
                TechnologyOperationComponentFields.CHILDREN)) {
            ComponentsCalculationHolder nextChild = allOperationsMap.get(toc.getId());
            addChildCost(component, nextChild, allOperationsMap, entitiesById, mathContext);
        }
    }
}
