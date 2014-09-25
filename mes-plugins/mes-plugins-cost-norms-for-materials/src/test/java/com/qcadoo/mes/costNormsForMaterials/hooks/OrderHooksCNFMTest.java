package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
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
        stubOrderState(OrderState.PENDING);
    }

    private void stubOrderState(final OrderState state) {
        Preconditions.checkArgument(state != null, "you have to pass not null OrderState!");
        stubStringField(order, OrderFields.STATE, state.getStringValue());
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
        stubOrderState(OrderState.PENDING);
        List<Entity> generatorResults = Lists.newArrayList(mockEntity(), mockEntity());
        stubGeneratorResults(generatorResults);

        // when
        orderHooksCNFM.fillOrderOperationProductsInComponents(orderDD, order);

        // then
        verify(order).setField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, generatorResults);
    }

    @Test
    public final void shouldNotRunGenerator() {
        // given & when
        for (OrderState orderState : Collections2.filter(Arrays.asList(OrderState.values()), not(equalTo(OrderState.PENDING)))) {
            stubOrderState(orderState);
            orderHooksCNFM.fillOrderOperationProductsInComponents(orderDD, order);
        }

        // then
        verify(order, never()).setField(eq(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS), anyList());
    }

}
