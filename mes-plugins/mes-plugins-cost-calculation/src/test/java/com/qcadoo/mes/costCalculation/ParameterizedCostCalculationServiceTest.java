package com.qcadoo.mes.costCalculation;

/*import static java.math.BigDecimal.valueOf;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;

 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;

 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;

 import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationServiceImpl;
 import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
 import com.qcadoo.model.api.Entity;

 @RunWith(Parameterized.class)
 public class ParameterizedCostCalculationServiceTest {

 private CostCalculationService costService;

 private Entity order;

 private Entity technology;

 Map<String, BigDecimal> results;

 private ProductsCostCalculationConstants validateIncludeCostOfMaterial;

 private OperationCostCalculationConstants validateIncludeCostOfOperation

 BigDecimal validateQuantity, validateMaterialCost, validateMachineHourlyCost, validateLaborHourlyCost,
 validateTotalTechnicalProductionCost, validateProductionCostMargin, validateProductionCostMarginValue,
 validateMaterialCostMargin, validateMaterialCostMarginValue, validateAdditionalOverhead, validateTotalOverhead,
 validateTotalCost, validateCostPerUnit ;

 Boolean validateIncludeTPZ;

 @Parameters
 public static Collection<Object[]> data() {
 return Arrays.asList(new Object[][] {
 // quantity,
 // materialCost,machineHourlyCost,laborHourlyCost,totalTechnicalProductionCost,productionCostMargin,productionCostMarginValue,materialCostMargin,materialCostMarginValue,
 // additionalOverhead,totalOverhead,totalCost,costPerUnit,includeTPZ,includeCostOfMaterial,includeCostOfOperation,
 { valueOf(10), valueOf(10), valueOf(10), valueOf(10), valueOf(10), valueOf(10), valueOf(10), valueOf(10),
 valueOf(10), valueOf(10), valueOf(10), valueOf(10), valueOf(10), true, ProductsCostCalculationConstants.NOMINAL, OperationCostCalculationConstants.HOURLY } });
 }

 public ParameterizedCostCalculationServiceTest(BigDecimal quantity, BigDecimal materialCost, BigDecimal machineHourlyCost,
 BigDecimal laborHourlyCost, BigDecimal totalTechnicalProductionCost, BigDecimal productionCostMargin,
 BigDecimal productionCostMarginValue, BigDecimal materialCostMargin, BigDecimal materialCostMarginValue,
 BigDecimal additionalOverhead, BigDecimal totalOverhead, BigDecimal totalCost, BigDecimal costPerUnit,
 boolean includeTPZ, BigDecimal includeCostOfMaterial, BigDecimal includeCostOfOperation) {
 this.validateQuantity = quantity;
 this.validateMaterialCost = materialCost;
 this.validateMachineHourlyCost = machineHourlyCost;
 this.validateLaborHourlyCost = laborHourlyCost;
 this.validateTotalTechnicalProductionCost = totalTechnicalProductionCost;
 this.validateProductionCostMargin = productionCostMargin;
 this.validateProductionCostMarginValue = productionCostMarginValue;
 this.validateMaterialCostMargin = materialCostMargin;
 this.validateMaterialCostMarginValue = materialCostMarginValue;
 this.validateAdditionalOverhead = additionalOverhead;
 this.validateTotalOverhead = totalOverhead;
 this.validateTotalCost = totalCost;
 this.validateCostPerUnit = costPerUnit;
 this.validateIncludeTPZ = includeTPZ;
 this.validateIncludeCostOfMaterial = includeCostOfMaterial;
 this.validateIncludeCostOfOperation = includeCostOfOperation;
 }

 @Before
 public void init() {
 costService = new CostCalculationServiceImpl();

 technology = mock(Entity.class);
 order = mock(Entity.class);
 results = mock(Map.class);

 when(results.get("quantity")).thenReturn(validateQuantity);
 when(results.get("productionCostMargin")).thenReturn(validateProductionCostMargin);
 when(results.get("materialCostMargin")).thenReturn(validateMaterialCostMargin);
 when(results.get("additionalOverhead")).thenReturn(validateAdditionalOverhead);


 }
 }*/
