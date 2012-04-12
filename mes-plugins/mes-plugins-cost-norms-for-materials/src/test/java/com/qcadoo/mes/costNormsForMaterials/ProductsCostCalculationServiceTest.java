package com.qcadoo.mes.costNormsForMaterials;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

public class ProductsCostCalculationServiceTest {

    private ProductsCostCalculationService productsCostCalculationService;

    @Mock
    private Entity costCalculation, product, order;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Mock
    private NumberService numberService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition technologyInstanceDD;

    private Map<Entity, BigDecimal> neededProductQuantities;

    @Before
    public void init() {
        productsCostCalculationService = new ProductsCostCalculationServiceImpl();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(productsCostCalculationService, "productQuantitiesService", productQuantitiesService);
        ReflectionTestUtils.setField(productsCostCalculationService, "numberService", numberService);
        ReflectionTestUtils.setField(productsCostCalculationService, "dataDefinitionService", dataDefinitionService);
        neededProductQuantities = new HashMap<Entity, BigDecimal>();
    }

    @Test
    public void shouldCalculateTotalProductsCostFromOrderMaterialCosts() throws Exception {
        // given
        String sourceOfMaterialCosts = "02fromOrdersMaterialCosts";
        String calculateMaterialCostsMode = "01nominal";

        when(costCalculation.getStringField("calculateMaterialCostsMode")).thenReturn(calculateMaterialCostsMode);
        // when
        productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);
    }

    @Test
    public void shouldCalculateTotalProductsCostFromCurrentGlobalDefinitionsInProduct() throws Exception {
        // given
        String sourceOfMaterialCosts = "01currentGlobalDefinitionsInProduct";
        String calculateMaterialCostsMode = "02average";

        when(costCalculation.getStringField("calculateMaterialCostsMode")).thenReturn(calculateMaterialCostsMode);
        // when
        productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenCalculateTotalProductsCostForIncorrectSourceO() throws Exception {
        // given
        String sourceOfMaterialCosts = "10incorect";
        String calculateMaterialCostsMode = "02average";

        when(costCalculation.getStringField("calculateMaterialCostsMode")).thenReturn(calculateMaterialCostsMode);
        // when
        productsCostCalculationService.calculateTotalProductsCost(costCalculation, sourceOfMaterialCosts);
    }

    @Test
    public void shouldCalculateProductCostForGivenQuantity() throws Exception {
        // given
        BigDecimal quantity = BigDecimal.TEN;
        BigDecimal costForNumber = BigDecimal.TEN;
        BigDecimal averageCost = BigDecimal.TEN;
        String calculateMaterialCostsMode = "02average";
        when(product.getField("averageCost")).thenReturn(averageCost);
        when(product.getField("costForNumber")).thenReturn(costForNumber);
        when(numberService.getMathContext()).thenReturn(MathContext.DECIMAL64);
        // when
        productsCostCalculationService.calculateProductCostForGivenQuantity(product, quantity, calculateMaterialCostsMode);
    }

    @Test
    public void shouldGetAppropriateCostNormForProductForCurrentGlobalDefinitionsInProduct() throws Exception {
        // given
        String sourceOfMaterialCosts = "01currentGlobalDefinitionsInProduct";
        // when
        productsCostCalculationService.getAppropriateCostNormForProduct(product, order, sourceOfMaterialCosts);
    }

}
