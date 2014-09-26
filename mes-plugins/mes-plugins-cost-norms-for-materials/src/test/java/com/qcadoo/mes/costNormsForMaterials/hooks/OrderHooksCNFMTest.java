package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OrderHooksCNFMTest {

    private OrderHooksCNFM orderHooksCNFM;

    @Mock
    private OrderMaterialsCostDataGenerator orderMaterialsCostDataGenerator;

    @Mock
    private Entity order;

    @Mock
    private DataDefinition orderDD;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderHooksCNFM = new OrderHooksCNFM();

        ReflectionTestUtils.setField(orderHooksCNFM, "orderMaterialsCostDataGenerator", orderMaterialsCostDataGenerator);
    }

    private void stubGeneratorResults(final List<Entity> generatedMaterialCostComponentsList) {
        given(orderMaterialsCostDataGenerator.generateUpdatedMaterialsListFor(order)).willAnswer(new Answer<List<Entity>>() {

            @Override
            public List<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(generatedMaterialCostComponentsList);
            }
        });
    }

    @Test
    public final void shouldRunGenerator() {
        // given
        List<Entity> generatorResults = Lists.newArrayList(mockEntity(), mockEntity());
        stubGeneratorResults(generatorResults);

        // when
        orderHooksCNFM.fillOrderOperationProductsInComponents(orderDD, order);

        // then
        verify(order).setField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, generatorResults);
    }

}
