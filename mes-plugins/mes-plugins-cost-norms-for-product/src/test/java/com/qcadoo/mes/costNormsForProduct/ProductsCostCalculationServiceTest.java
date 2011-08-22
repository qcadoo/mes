package com.qcadoo.mes.costNormsForProduct;

import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;

public class ProductsCostCalculationServiceTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNull() {
        // given
        ProductsCostCalculationService productCostCalc = new ProductsCostCalculationServiceImpl();

        // when
        productCostCalc.calculateProductsCost(ProductsCostCalculationConstants.AVERAGE, null);

        // then
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenModeIsIncorrect() {
        // given
        Entity technology = mock(Entity.class);
        ProductsCostCalculationService productCostCalc = new ProductsCostCalculationServiceImpl();

        // when
        productCostCalc.calculateProductsCost("SomeIncorrectValue", technology);

        // then
    }
    //
    // @Test
    // public void shouldReturnCorrectValues() throws Exception {
    // //given
    // Entity technology = mock(Entity.class);
    // EntityTree operationComponents = mock(EntityTree.class);
    // Entity operationComponent = mock(Entity.class);
    // EntityList inputProducts = mock(EntityList.class);
    // Entity product = mock(Entity.class);
    //
    // when(technology.getTreeField("operationComponents")).thenReturn(operationComponents);
    // when(operationComponents.get(0)).thenReturn(operationComponent);
    // when(operationComponent.getHasManyField("operationProductInComponents")).thenReturn(inputProducts);
    // when(inputProducts.get(0)).thenReturn(product);
    // when(inputProducts.get(1)).thenReturn(product);
    // when(inputProducts.get(2)).thenReturn(product);
    //
    // when(product.getField(ProductsCostCalculationConstants.AVERAGE)).thenReturn(15);
    // when(product.getField(ProductsCostCalculationConstants.LAST_PURCHASE)).thenReturn(20);
    // when(product.getField(ProductsCostCalculationConstants.NOMINAL)).thenReturn(10);
    //
    // ProductsCostCalculationService productCostCalc = new ProductsCostCalculationServiceImpl();
    //
    // //when
    // BigDecimal result = productCostCalc.calculateProductsCost(ProductsCostCalculationConstants.AVERAGE, technology);
    //
    // //then
    // assertEquals(result, BigDecimal.valueOf(45));
    //
    // }

}
