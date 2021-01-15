package com.qcadoo.mes.costCalculation.print;

import com.google.common.collect.Lists;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CostCalculationComponentsService {

    private static final String L_COMPONENT = "component";

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private NumberService numberService;

    public void addMaterialOperationCost(final Entity costCalculation, final List<ComponentsCalculationHolder> basicComponents,
            final Map<OperationProductComponentHolder, BigDecimal> materialQuantitiesByOPC) {
        MathContext mathContext = numberService.getMathContext();
        for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : materialQuantitiesByOPC.entrySet()) {
            Entity product = neededProductQuantity.getKey().getProduct();

            BigDecimal productQuantity = neededProductQuantity.getValue();

            BigDecimal costForGivenQuantity = productsCostCalculationService.calculateProductCostForGivenQuantity(product,
                    productQuantity, costCalculation.getStringField(CostCalculationFields.MATERIAL_COSTS_USED),
                    costCalculation.getBooleanField(CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED));

            ComponentsCalculationHolder cc = basicComponents.stream()
                    .filter(bc -> bc.getToc().getId().equals(neededProductQuantity.getKey().getTechnologyOperationComponentId()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(cc)) {
                BigDecimal materialCost = BigDecimalUtils.convertNullToZero(cc.getMaterialCost())
                        .add(BigDecimalUtils.convertNullToZero(costForGivenQuantity), mathContext);
                cc.setMaterialCost(numberService.setScaleWithDefaultMathContext(materialCost, 2));
            }

        }
    }

    public void addOperationCost(final Entity costCalculation, List<ComponentsCalculationHolder> components) {
        MathContext mathContext = numberService.getMathContext();

        List<Entity> calculationOperationComponents = costCalculation
                .getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS);

        for (Entity calculationOperationComponent : calculationOperationComponents) {
            BigDecimal totalMachineOperationCost = calculationOperationComponent
                    .getDecimalField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST);
            BigDecimal totalLaborOperationCost = calculationOperationComponent
                    .getDecimalField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST);
            ComponentsCalculationHolder holder = components.stream()
                    .filter(bc -> bc.getToc().getId()
                            .equals(calculationOperationComponent.getBelongsToField("technologyOperationComponent").getId()))
                    .findFirst().orElse(null);

            if (Objects.nonNull(holder)) {
                BigDecimal cost = BigDecimalUtils.convertNullToZero(totalMachineOperationCost)
                        .add(BigDecimalUtils.convertNullToZero(totalLaborOperationCost), mathContext);
                holder.setLaborCost(numberService.setScaleWithDefaultMathContext(cost, 2));
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
        List<Entity> tocs = operationComponents.stream()
                .filter(pc -> L_COMPONENT.equals(pc.getStringField(TechnologyOperationComponentFields.TYPE_FROM_STRUCTURE_TREE)))
                .collect(Collectors.toList());
        for (Entity toc : tocs) {
            ComponentsCalculationHolder component = new ComponentsCalculationHolder(toc,
                    toc.getBelongsToField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE));
            basicComponents.add(component);
        }
        return basicComponents;
    }

    public void fillBasicComponentsCosts(final EntityTree operationComponents,
            final List<ComponentsCalculationHolder> basicComponents, final List<ComponentsCalculationHolder> allOperations,
            final BigDecimal quantity) {

        Map<Long, Entity> entitiesById = new LinkedHashMap<Long, Entity>();
        MathContext mathContext = numberService.getMathContext();

        for (Entity entity : operationComponents) {
            entitiesById.put(entity.getId(), entity);
        }

        for (ComponentsCalculationHolder component : basicComponents) {
            ComponentsCalculationHolder holder = allOperations.stream()
                    .filter(op -> op.getToc().getId().equals(component.getToc().getId())).findFirst().get();
            component.setMaterialCost(holder.getMaterialCost());
            component.setLaborCost(holder.getLaborCost());

            addChildCost(component, null, allOperations, entitiesById, mathContext);

            BigDecimal sumOfCost = BigDecimalUtils.convertNullToZero(component.getLaborCost())
                    .add(BigDecimalUtils.convertNullToZero(component.getMaterialCost()), mathContext);
            BigDecimal costPerUnit = sumOfCost.divide(quantity, mathContext);
            component.setSumOfCost(numberService.setScaleWithDefaultMathContext(sumOfCost, 2));
            component.setCostPerUnit(numberService.setScaleWithDefaultMathContext(costPerUnit, 2));
        }

    }

    private void addChildCost(ComponentsCalculationHolder component, ComponentsCalculationHolder child,
            List<ComponentsCalculationHolder> allOperations, Map<Long, Entity> entitiesById, MathContext mathContext) {
        ComponentsCalculationHolder _component = component;

        if (child != null) {
            BigDecimal materialCost = BigDecimalUtils.convertNullToZero(component.getMaterialCost())
                    .add(BigDecimalUtils.convertNullToZero(child.getMaterialCost()), mathContext);
            BigDecimal laborCost = BigDecimalUtils.convertNullToZero(component.getLaborCost())
                    .add(BigDecimalUtils.convertNullToZero(child.getLaborCost()), mathContext);
            component.setLaborCost(numberService.setScaleWithDefaultMathContext(laborCost, 2));
            component.setMaterialCost(numberService.setScaleWithDefaultMathContext(materialCost, 2));
            _component = child;
        }

        for (Entity toc : entitiesById.get(_component.getToc().getId())
                .getHasManyField(TechnologyOperationComponentFields.CHILDREN)) {
            ComponentsCalculationHolder nextChild = allOperations.stream().filter(op -> op.getToc().getId().equals(toc.getId()))
                    .findFirst().orElse(null);
            addChildCost(component, nextChild, allOperations, entitiesById, mathContext);
        }
    }
}
