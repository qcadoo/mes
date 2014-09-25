package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithCosts;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class OrderMaterialCostsEntityBuilderImplTest {

    private OrderMaterialCostsEntityBuilder orderMaterialCostsEntityBuilder;

    @Mock
    private Entity createdEntity;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        DataDefinition materialCostsComponentDD = mock(DataDefinition.class);
        given(materialCostsComponentDD.create()).willReturn(createdEntity);

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        given(
                dataDefinitionService.get(CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                        CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP)).willReturn(
                materialCostsComponentDD);

        orderMaterialCostsEntityBuilder = new OrderMaterialCostsEntityBuilderImpl();

        ReflectionTestUtils.setField(orderMaterialCostsEntityBuilder, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public final void shouldBuildEntity() {
        // given
        Entity order = mockEntity();
        ProductWithCosts productWithCosts = new ProductWithCosts(1L, BigDecimal.valueOf(2L), BigDecimal.valueOf(3L),
                BigDecimal.valueOf(4L), BigDecimal.valueOf(5L));

        // when
        orderMaterialCostsEntityBuilder.create(order, productWithCosts);

        // then
        verify(createdEntity).setField(TechnologyInstOperProductInCompFields.ORDER, order);
        verify(createdEntity).setField(TechnologyInstOperProductInCompFields.PRODUCT, productWithCosts.getProductId());
        verify(createdEntity)
                .setField(TechnologyInstOperProductInCompFields.COST_FOR_NUMBER, productWithCosts.getCostForNumber());
        verify(createdEntity).setField(TechnologyInstOperProductInCompFields.NOMINAL_COST, productWithCosts.getNominalCost());
        verify(createdEntity).setField(TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST,
                productWithCosts.getLastPurchaseCost());
        verify(createdEntity).setField(TechnologyInstOperProductInCompFields.AVERAGE_COST, productWithCosts.getAverageCost());
    }
}
