package com.qcadoo.mes.costNormsForProduct;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.AVERAGE;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.LAST_PURCHASE;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.NOMINAL;
import static java.math.BigDecimal.valueOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

//@RunWith(Parameterized.class)
public class ProductsCostCalculationServiceTest {
/*
    private ProductsCostCalculationService productCostCalc;
    private Entity technology;
    private ProductsCostCalculationConstants validationMode;
    private BigDecimal validationAverage, 
                        validationLastPurchase, 
                        validationNominal, 
                        validationInputQuantity,
                        validationOrderQuantity,
                        validationExpectedResult;
    private Integer validationCostForNumber;
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // mode,  average,     lastPurchase, nominal,     costForNumber, input qtty, order qtty, expectedResult
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 1,             valueOf(1), valueOf(1), valueOf(30)},
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 1,             valueOf(2), valueOf(1), valueOf(60)},
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 1,             valueOf(3), valueOf(1), valueOf(90)},
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 1,             valueOf(3), valueOf(2), valueOf(180)},
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 1,             valueOf(3), valueOf(3), valueOf(270)},
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 1,             valueOf(3), valueOf(4), valueOf(360)},
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 2,             valueOf(3), valueOf(2), valueOf(90)},
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 2,             valueOf(3), valueOf(3), valueOf(135)},
                {AVERAGE, valueOf(10), valueOf(5),   valueOf(15), 2,             valueOf(3), valueOf(4), valueOf(180)},
        });
    }
    
    public ProductsCostCalculationServiceTest(ProductsCostCalculationConstants mode, 
            BigDecimal average, BigDecimal lastPurchase, BigDecimal nominal, Integer costForNumber, BigDecimal inputQuantity, 
            BigDecimal orderQuantity, BigDecimal expectedResult) {
        this.validationAverage = average;
        this.validationExpectedResult = expectedResult;
        this.validationInputQuantity = inputQuantity;
        this.validationLastPurchase = lastPurchase;
        this.validationMode = mode;
        this.validationNominal = nominal;
        this.validationOrderQuantity = orderQuantity;
        this.validationCostForNumber = costForNumber;
    }
    
    @Before
    public void init() {
        productCostCalc = new ProductsCostCalculationServiceImpl();

        technology = mock(Entity.class);
        EntityTree operationComponents = mock(EntityTree.class);
        Entity operationComponent = mock(Entity.class);
        EntityList inputProducts = mock(EntityList.class);
        Entity inputProduct = mock(Entity.class);
        Entity product = mock(Entity.class);

        when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);
        when(operationComponents.get(0)).thenReturn(operationComponent);
        when(operationComponent.getHasManyField("operationProductInComponents")).thenReturn(inputProducts);
        
        when(inputProducts.get(0)).thenReturn(inputProduct);
        when(inputProducts.get(1)).thenReturn(inputProduct);
        when(inputProducts.get(2)).thenReturn(inputProduct);
        
        when(inputProduct.getField("quantity")).thenReturn(validationInputQuantity);
        when(inputProduct.getBelongsToField("product")).thenReturn(product);

        when(product.getField(AVERAGE.getStrValue())).thenReturn(validationAverage);
        when(product.getField(LAST_PURCHASE.getStrValue())).thenReturn(validationLastPurchase);
        when(product.getField(NOMINAL.getStrValue())).thenReturn(validationNominal);
        when(product.getField("costForNumber")).thenReturn(validationCostForNumber);
        
    }

    @Test
    public void shouldReturnCorrectCostValuesUsingTechnology() throws Exception {
        // when
        BigDecimal result = productCostCalc.calculateProductsCost(technology, validationMode, validationOrderQuantity);

        // then
        assertEquals(validationExpectedResult, result);
    }
    */
}
