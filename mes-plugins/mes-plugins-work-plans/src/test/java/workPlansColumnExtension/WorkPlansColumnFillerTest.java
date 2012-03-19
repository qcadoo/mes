package workPlansColumnExtension;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.workPlans.workPlansColumnExtension.WorkPlansColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

public class WorkPlansColumnFillerTest {

    private WorkPlansColumnFiller workPlansColumnFiller;

    @Mock
    private Entity order, order2, product;

    @Mock
    private NumberService numberService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        workPlansColumnFiller = new WorkPlansColumnFiller();

        given(numberService.format(Mockito.any(BigDecimal.class))).willAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                BigDecimal number = (BigDecimal) args[0];
                return number.setScale(3, RoundingMode.HALF_EVEN).toString();
            }
        });

        ReflectionTestUtils.setField(workPlansColumnFiller, "numberService", numberService);

        given(order.getStringField("name")).willReturn("order");
        given(order.getStringField("number")).willReturn("1234");
        given(order.getField("plannedQuantity")).willReturn(new BigDecimal(11));
        given(order2.getStringField("name")).willReturn("order2");

        given(order.getBelongsToField("product")).willReturn(product);
        given(order2.getBelongsToField("product")).willReturn(product);

        given(product.getStringField("name")).willReturn("product");
        given(product.getStringField("number")).willReturn("123");
        given(product.getStringField("unit")).willReturn("abc");
    }

    @Test
    public void shouldReturnCorrectColumnValuesForOrders() {
        // given
        List<Entity> orders = Arrays.asList(order, order2);

        // when
        Map<Entity, Map<String, String>> orderValues = workPlansColumnFiller.getOrderValues(orders);

        // then
        assertEquals(2, orderValues.size());
        assertEquals(5, orderValues.get(order).size());
        assertEquals(5, orderValues.get(order2).size());
        assertEquals("order", orderValues.get(order).get("orderName"));
        assertEquals("1234", orderValues.get(order).get("orderNumber"));
        assertEquals("product (123)", orderValues.get(order).get("productName"));
        assertEquals("11.000 abc", orderValues.get(order).get("plannedQuantity"));
    }
}
