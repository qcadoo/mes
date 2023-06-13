package com.qcadoo.mes.costCalculation.print;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.mes.costCalculation.print.dto.ComponentCostKey;
import com.qcadoo.mes.costCalculation.print.dto.ComponentsCalculationHolder;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.technologies.ProductQuantitiesWithComponentsService;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;

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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private void addMaterialCost(final Entity costCalculation, final List<ComponentsCalculationHolder> allOperationComponents,
                                 final Entity technology, final BigDecimal quantity) {
        MathContext mathContext = numberService.getMathContext();
        Map<OperationProductComponentHolder, BigDecimal> materialQuantitiesByOPC = productQuantitiesWithComponentsService
                .getNeededProductQuantitiesByOPC(technology, quantity, MrpAlgorithm.ONLY_MATERIALS);
        DataDefinition operationProductComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
        Entity offer = costCalculation.getBelongsToField(CostCalculationFields.OFFER);
        for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : materialQuantitiesByOPC.entrySet()) {
            Entity product = neededProductQuantity.getKey().getProduct();
            Entity operationProductComponent = operationProductComponentDD.get(neededProductQuantity.getKey()
                    .getOperationProductComponentId());
            BigDecimal costPerUnit = productsCostCalculationService.calculateOperationProductCostPerUnit(costCalculation,
                    product, operationProductComponent);

            BigDecimal productQuantity = neededProductQuantity.getValue();
            BigDecimal costForGivenQuantity = costPerUnit.multiply(BigDecimalUtils.convertNullToZero(productQuantity),
                    numberService.getMathContext());
            if (operationProductComponent
                    .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES)) {
                List<Entity> productsBySize = operationProductComponent
                        .getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS);
                BigDecimal sumOfCosts = BigDecimal.ZERO;

                for (Entity pbs : productsBySize) {
                    Entity p = pbs.getBelongsToField(ProductBySizeGroupFields.PRODUCT);

                    BigDecimal costPerUnitPBS = productsCostCalculationService.calculateProductCostPerUnit(p,
                            costCalculation.getStringField(CostCalculationFields.MATERIAL_COSTS_USED),
                            costCalculation.getBooleanField(CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED),
                            offer);
                    BigDecimal q = costCalculation.getDecimalField(CostCalculationFields.QUANTITY).multiply(
                            pbs.getDecimalField(ProductBySizeGroupFields.QUANTITY), numberService.getMathContext());

                    BigDecimal costPBS = numberService.setScaleWithDefaultMathContext(costPerUnitPBS.multiply(q));
                    sumOfCosts = sumOfCosts.add(costPBS, numberService.getMathContext());

                }
                costForGivenQuantity = sumOfCosts.divide(new BigDecimal(productsBySize.size()), numberService.getMathContext());
            }
            ComponentsCalculationHolder cc = allOperationComponents.stream()
                    .filter(bc -> bc.getToc().getId().equals(neededProductQuantity.getKey().getTechnologyOperationComponentId()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(cc)) {
                BigDecimal materialCost = BigDecimalUtils.convertNullToZero(cc.getMaterialCost()).add(
                        BigDecimalUtils.convertNullToZero(costForGivenQuantity), mathContext);
                cc.setMaterialCost(numberService.setScaleWithDefaultMathContext(materialCost, 2));
            }
        }
    }

    private void addLaborCost(final Entity costCalculation, List<ComponentsCalculationHolder> allOperationComponents,
                              List<Entity> calculationOperationComponents) {
        if (!SourceOfOperationCosts.STANDARD_LABOR_COSTS.equals(SourceOfOperationCosts.parseString(costCalculation
                .getStringField(CostCalculationFields.SOURCE_OF_OPERATION_COSTS)))) {

            for (Entity calculationOperationComponent : calculationOperationComponents) {
                Entity toc = calculationOperationComponent.getBelongsToField(
                        CalculationOperationComponentFields.TECHNOLOGY_OPERATION_COMPONENT);
                if (!toc.getBooleanField(TechnologyOperationComponentFieldsTNFO.PIECEWORK_PRODUCTION)) {
                    BigDecimal totalMachineOperationCost = calculationOperationComponent
                            .getDecimalField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST);
                    BigDecimal totalLaborOperationCost = calculationOperationComponent
                            .getDecimalField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST);
                    ComponentsCalculationHolder holder = allOperationComponents
                            .stream()
                            .filter(bc -> bc
                                    .getToc()
                                    .getId()
                                    .equals(toc.getId())).findFirst()
                            .orElse(null);

                    if (Objects.nonNull(holder)) {
                        BigDecimal cost = BigDecimalUtils.convertNullToZero(totalMachineOperationCost).add(
                                BigDecimalUtils.convertNullToZero(totalLaborOperationCost), numberService.getMathContext());
                        holder.setLaborCost(numberService.setScaleWithDefaultMathContext(cost, 2));
                    }
                }
            }
        }
    }

    public Collection<ComponentsCalculationHolder> getComponentCosts(final Entity costCalculation, final Entity technology,
                                                                     List<Entity> calculationOperationComponents) {
        EntityTree operationComponents = productStructureTreeService.getOperationComponentsFromTechnology(technology);
        List<ComponentsCalculationHolder> components = operationComponents
                .stream()
                .filter(pc -> ProductStructureTreeService.L_COMPONENT.equals(pc
                        .getStringField(TechnologyOperationComponentFields.TYPE_FROM_STRUCTURE_TREE)))
                .map(toc -> new ComponentsCalculationHolder(toc, toc
                        .getBelongsToField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE), technology))
                .collect(Collectors.toList());
        List<ComponentsCalculationHolder> allOperationComponents = operationComponents
                .stream()
                .map(toc -> new ComponentsCalculationHolder(toc, toc
                        .getBelongsToField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE), technology))
                .collect(Collectors.toList());
        BigDecimal quantity = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);
        addMaterialCost(costCalculation, allOperationComponents, technology, quantity);
        addLaborCost(costCalculation, allOperationComponents, calculationOperationComponents);
        fillComponentsQuantity(components, technology, quantity);
        fillComponentsCosts(operationComponents, components, allOperationComponents, quantity);
        fillAdditionalProductsMark(components);
        return groupComponentCosts(components);
    }

    private void fillAdditionalProductsMark(List<ComponentsCalculationHolder> components) {
        for (ComponentsCalculationHolder component : components) {
            List<Entity> outProducts = component.getToc()
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS)
                    .stream().filter(op -> !op.getBooleanField(OperationProductOutComponentFields.WASTE)).collect(Collectors.toList());

           if (outProducts.size() > 1) {
               component.setAdditionalProducts(true);
           }
        }

    }

    private Collection<ComponentsCalculationHolder> groupComponentCosts(List<ComponentsCalculationHolder> components) {
        Map<ComponentCostKey, ComponentsCalculationHolder> groupedComponentCosts = new HashMap<>();
        for (ComponentsCalculationHolder componentsCalculationHolder : components) {
            ComponentCostKey componentCostKey = new ComponentCostKey(componentsCalculationHolder.getProduct().getId(),
                    componentsCalculationHolder.getTechnologyInputProductType());
            if (groupedComponentCosts.containsKey(componentCostKey)) {
                ComponentsCalculationHolder groupedComponentCost = groupedComponentCosts.get(componentCostKey);
                groupedComponentCost.setMaterialCost(groupedComponentCost.getMaterialCost().add(
                        componentsCalculationHolder.getMaterialCost(), numberService.getMathContext()));
                if (!Objects.isNull(groupedComponentCost.getLaborCost())) {
                    groupedComponentCost.setLaborCost(groupedComponentCost.getLaborCost().add(
                            componentsCalculationHolder.getLaborCost(), numberService.getMathContext()));
                }
                groupedComponentCost.setSumOfCost(groupedComponentCost.getSumOfCost().add(
                        componentsCalculationHolder.getSumOfCost(), numberService.getMathContext()));
                groupedComponentCost.setQuantity(groupedComponentCost.getQuantity().add(
                        componentsCalculationHolder.getQuantity(), numberService.getMathContext()));
                groupedComponentCost.setCostPerUnit(groupedComponentCost.getCostPerUnit().add(
                        componentsCalculationHolder.getCostPerUnit(), numberService.getMathContext()));
                groupedComponentCosts.put(componentCostKey, groupedComponentCost);
            } else {
                groupedComponentCosts.put(componentCostKey, componentsCalculationHolder);
            }
        }
        return groupedComponentCosts.values();
    }

    private void fillComponentsQuantity(List<ComponentsCalculationHolder> components, Entity technology, BigDecimal quantity) {
        Map<OperationProductComponentHolder, BigDecimal> componentQuantitiesByOPC = productQuantitiesWithComponentsService
                .getNeededProductQuantitiesByOPC(technology, quantity, MrpAlgorithm.ALL_PRODUCTS_IN);
        DataDefinition operationProductComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
        for (ComponentsCalculationHolder component : components) {
            for (Map.Entry<OperationProductComponentHolder, BigDecimal> cq : componentQuantitiesByOPC.entrySet()) {
                if (cq.getKey().getTechnologyOperationComponentId()
                        .equals(component.getToc().getBelongsToField(TechnologyOperationComponentFields.PARENT).getId())
                        && cq.getKey().getProductId().equals(component.getProduct().getId())) {
                    component.setQuantity(cq.getValue());
                    String technologyInputProductTypeName = "";
                    Entity technologyInputProductType = operationProductComponentDD.get(
                            cq.getKey().getOperationProductComponentId()).getBelongsToField(
                            OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
                    if (technologyInputProductType != null) {
                        technologyInputProductTypeName = technologyInputProductType
                                .getStringField(TechnologyInputProductTypeFields.NAME);
                    }
                    component.setTechnologyInputProductType(technologyInputProductTypeName);
                    break;
                }
            }
        }
    }

    private void fillComponentsCosts(final EntityTree operationComponents, final List<ComponentsCalculationHolder> components,
                                     final List<ComponentsCalculationHolder> allOperationComponents, final BigDecimal quantity) {
        Map<Long, Entity> entitiesById = new LinkedHashMap<>();

        for (Entity entity : operationComponents) {
            entitiesById.put(entity.getId(), entity);
        }

        for (ComponentsCalculationHolder component : components) {
            ComponentsCalculationHolder holder = allOperationComponents.stream()
                    .filter(op -> op.getToc().getId().equals(component.getToc().getId())).findFirst().get();
            component.setMaterialCost(holder.getMaterialCost());
            component.setLaborCost(holder.getLaborCost());

            addChildCost(component, null, allOperationComponents, entitiesById, numberService.getMathContext());

            BigDecimal sumOfCost = BigDecimalUtils.convertNullToZero(component.getLaborCost()).add(
                    BigDecimalUtils.convertNullToZero(component.getMaterialCost()), numberService.getMathContext());
            BigDecimal costPerUnit = sumOfCost.divide(quantity, numberService.getMathContext());
            component.setSumOfCost(numberService.setScaleWithDefaultMathContext(sumOfCost, 2));
            component.setCostPerUnit(numberService.setScaleWithDefaultMathContext(costPerUnit, 2));
        }
    }

    private void addChildCost(ComponentsCalculationHolder component, ComponentsCalculationHolder child,
                              List<ComponentsCalculationHolder> allOperations, Map<Long, Entity> entitiesById, MathContext mathContext) {
        ComponentsCalculationHolder _component = component;

        if (child != null) {
            BigDecimal materialCost = BigDecimalUtils.convertNullToZero(component.getMaterialCost()).add(
                    BigDecimalUtils.convertNullToZero(child.getMaterialCost()), mathContext);
            BigDecimal laborCost = BigDecimalUtils.convertNullToZero(component.getLaborCost()).add(
                    BigDecimalUtils.convertNullToZero(child.getLaborCost()), mathContext);
            component.setLaborCost(numberService.setScaleWithDefaultMathContext(laborCost, 2));
            component.setMaterialCost(numberService.setScaleWithDefaultMathContext(materialCost, 2));
            _component = child;
        }

        for (Entity toc : entitiesById.get(_component.getToc().getId()).getHasManyField(
                TechnologyOperationComponentFields.CHILDREN)) {
            ComponentsCalculationHolder nextChild = allOperations.stream().filter(op -> op.getToc().getId().equals(toc.getId()))
                    .findFirst().orElse(null);
            addChildCost(component, nextChild, allOperations, entitiesById, mathContext);
        }
    }
}
