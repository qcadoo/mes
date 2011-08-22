package com.qcadoo.mes.costNormsForProduct;

import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.AVERAGE;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.LAST_PURCHASE;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.NOMINAL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

public class ProductsCostCalculationServiceTest {

    private ProductsCostCalculationService productCostCalc;

    private Entity technology;

    private Entity product;

    @Before
    public void init() {
        productCostCalc = new ProductsCostCalculationServiceImpl();

        technology = mock(Entity.class);
        EntityTree operationComponents = mock(EntityTree.class);
        Entity operationComponent = mock(Entity.class);
        EntityList inputProducts = mock(EntityList.class);
        product = mock(Entity.class);

        when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);
        when(operationComponents.get(0)).thenReturn(operationComponent);
        when(operationComponent.getHasManyField("operationProductInComponents")).thenReturn(inputProducts);
        when(inputProducts.get(0)).thenReturn(product);
        when(inputProducts.get(1)).thenReturn(product);
        when(inputProducts.get(2)).thenReturn(product);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNull() {
        // when
        productCostCalc.calculateProductsCost(null, AVERAGE, new BigDecimal(1));
    }

    @Test
    public void shouldReturnCorrectCostValues() throws Exception {
        // given
        when(product.getField(AVERAGE.getStrValue())).thenReturn(15);
        when(product.getField(LAST_PURCHASE.getStrValue())).thenReturn(20);
        when(product.getField(NOMINAL.getStrValue())).thenReturn(10);

        // when
        BigDecimal averageResult = productCostCalc.calculateProductsCost(technology, AVERAGE, new BigDecimal(1));
        BigDecimal nominalResult = productCostCalc.calculateProductsCost(technology, NOMINAL, new BigDecimal(1));
        BigDecimal lastPurchaseResult = productCostCalc.calculateProductsCost(technology, LAST_PURCHASE, new BigDecimal(1));

        // then
        assertEquals(averageResult, BigDecimal.valueOf(45));
        assertEquals(nominalResult, BigDecimal.valueOf(30));
        assertEquals(lastPurchaseResult, BigDecimal.valueOf(60));
    }

}
