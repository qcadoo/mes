package com.qcadoo.mes.costCalculation.print;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.technologies.ProductQuantitiesWithComponentsService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyInputProductTypeFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

@Service
public class CostCalculationComponentsService {

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private ProductQuantitiesWithComponentsService productQuantitiesWithComponentsService;

    @Autowired
    private NumberService numberService;

    private void addMaterialCost(final Entity costCalculation, final List<ComponentsCalculationHolder> allOperationComponents,
            final Entity technology, final BigDecimal quantity) {
        MathContext mathContext = numberService.getMathContext();
        Map<OperationProductComponentHolder, BigDecimal> materialQuantitiesByOPC = productQuantitiesWithComponentsService
                .getNeededProductQuantitiesByOPC(technology, quantity, MrpAlgorithm.ONLY_MATERIALS);
        for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : materialQuantitiesByOPC.entrySet()) {
            Entity product = neededProductQuantity.getKey().getProduct();

            BigDecimal productQuantity = neededProductQuantity.getValue();

            BigDecimal costForGivenQuantity = productsCostCalculationService.calculateProductCostForGivenQuantity(product,
                    productQuantity, costCalculation.getStringField(CostCalculationFields.MATERIAL_COSTS_USED),
                    costCalculation.getBooleanField(CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED));

            ComponentsCalculationHolder cc = allOperationComponents.stream()
                    .filter(bc -> bc.getToc().getId().equals(neededProductQuantity.getKey().getTechnologyOperationComponentId()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(cc)) {
                BigDecimal materialCost = BigDecimalUtils.convertNullToZero(cc.getMaterialCost())
                        .add(BigDecimalUtils.convertNullToZero(costForGivenQuantity), mathContext);
                cc.setMaterialCost(numberService.setScaleWithDefaultMathContext(materialCost, 2));
            }
        }
    }

    private void addLaborCost(final Entity costCalculation, List<ComponentsCalculationHolder> allOperationComponents) {
        MathContext mathContext = numberService.getMathContext();

        List<Entity> calculationOperationComponents = costCalculation
                .getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS);

        for (Entity calculationOperationComponent : calculationOperationComponents) {
            BigDecimal totalMachineOperationCost = calculationOperationComponent
                    .getDecimalField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST);
            BigDecimal totalLaborOperationCost = calculationOperationComponent
                    .getDecimalField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST);
            ComponentsCalculationHolder holder = allOperationComponents.stream()
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

    public List<ComponentsCalculationHolder> getComponentCosts(final Entity costCalculation, final Entity technology) {
        EntityTree operationComponents = productStructureTreeService.getOperationComponentsFromTechnology(technology);
        List<ComponentsCalculationHolder> components = operationComponents.stream()
                .filter(pc -> ProductStructureTreeService.L_COMPONENT
                        .equals(pc.getStringField(TechnologyOperationComponentFields.TYPE_FROM_STRUCTURE_TREE)))
                .map(toc -> new ComponentsCalculationHolder(toc,
                        toc.getBelongsToField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE)))
                .collect(Collectors.toList());
        List<ComponentsCalculationHolder> allOperationComponents = operationComponents.stream()
                .map(toc -> new ComponentsCalculationHolder(toc,
                        toc.getBelongsToField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE)))
                .collect(Collectors.toList());
        BigDecimal quantity = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);
        addMaterialCost(costCalculation, allOperationComponents, technology, quantity);
        addLaborCost(costCalculation, allOperationComponents);
        fillComponentsQuantity(components, technology, quantity);
        fillComponentsCosts(operationComponents, components, allOperationComponents, quantity);
        return components;
    }

    private void fillComponentsQuantity(List<ComponentsCalculationHolder> components, Entity technology, BigDecimal quantity) {
        Map<OperationProductComponentHolder, BigDecimal> componentQuantitiesByOPC = productQuantitiesWithComponentsService
                .getNeededProductQuantitiesByOPC(technology, quantity, MrpAlgorithm.ALL_PRODUCTS_IN);
        for (ComponentsCalculationHolder component : components) {
            componentQuantitiesByOPC.entrySet().stream()
                    .filter(cq -> cq.getKey().getTechnologyOperationComponentId()
                            .equals(component.getToc().getBelongsToField(TechnologyOperationComponentFields.PARENT).getId())
                            && cq.getKey().getProductId().equals(component.getProduct().getId()))
                    .findFirst().ifPresent(v -> component.setQuantity(v.getValue()));
        }
    }

    private void fillComponentsCosts(final EntityTree operationComponents, final List<ComponentsCalculationHolder> components,
            final List<ComponentsCalculationHolder> allOperationComponents, final BigDecimal quantity) {

        Map<Long, Entity> entitiesById = new LinkedHashMap<>();
        MathContext mathContext = numberService.getMathContext();

        for (Entity entity : operationComponents) {
            entitiesById.put(entity.getId(), entity);
        }

        for (ComponentsCalculationHolder component : components) {
            ComponentsCalculationHolder holder = allOperationComponents.stream()
                    .filter(op -> op.getToc().getId().equals(component.getToc().getId())).findFirst().get();
            component.setMaterialCost(holder.getMaterialCost());
            component.setLaborCost(holder.getLaborCost());
            component.setTechnologyInputProductType(findTechnologyInputProductType(component));

            addChildCost(component, null, allOperationComponents, entitiesById, mathContext);

            BigDecimal sumOfCost = BigDecimalUtils.convertNullToZero(component.getLaborCost())
                    .add(BigDecimalUtils.convertNullToZero(component.getMaterialCost()), mathContext);
            BigDecimal costPerUnit = sumOfCost.divide(quantity, mathContext);
            component.setSumOfCost(numberService.setScaleWithDefaultMathContext(sumOfCost, 2));
            component.setCostPerUnit(numberService.setScaleWithDefaultMathContext(costPerUnit, 2));
        }

    }

    private String findTechnologyInputProductType(ComponentsCalculationHolder component) {
        String technologyInputProductTypeName = "";
        for (Entity opic : component.getToc().getBelongsToField(TechnologyOperationComponentFields.PARENT)
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)) {
            if (opic.getBelongsToField(OperationProductInComponentFields.PRODUCT).getId()
                    .equals(component.getProduct().getId())) {
                Entity technologyInputProductType = opic
                        .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
                if (technologyInputProductType != null) {
                    technologyInputProductTypeName = technologyInputProductType
                            .getStringField(TechnologyInputProductTypeFields.NAME);
                }
                break;
            }
        }
        return technologyInputProductTypeName;
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
