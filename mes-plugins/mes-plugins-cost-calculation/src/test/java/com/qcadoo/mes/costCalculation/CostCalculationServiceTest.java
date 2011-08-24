package com.qcadoo.mes.costCalculation;

/*import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;

 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;

 import org.junit.Before;
 import org.junit.Test;

 import com.qcadoo.model.api.Entity;
 import com.qcadoo.view.api.ComponentState;
 import com.qcadoo.view.api.ViewDefinitionState;

 public class CostCalculationServiceTest {

 private CostCalculationService costCalc;

 // private ProductsCostCalculationService productsCostCalculationService;

 private Entity technology;

 private Entity order;

 private Map hashMap;

 private ViewDefinitionState viewDefinitionState;

 private ComponentState state;

 @Before
 public void init() {
 costCalc = new CostCalculationServiceImpl();
 order = mock(Entity.class);
 technology = mock(Entity.class);
 hashMap = mock(Map.class);
 viewDefinitionState = mock(ViewDefinitionState.class);
 state = mock(ComponentState.class);
 }

 @Test(expected = IllegalArgumentException.class)
 public void shouldReturnExceptionWhenEntityIsNull() {
 // when
 costCalc.calculateTotalCost(technology, order, hashMap);

 }

 @Test
 public void shouldReturnCorrectValue() throws Exception {
 // given
 hashMap = new HashMap<String, Object>();
 hashMap.put("productionCostMargin", new BigDecimal(5));
 // when
 BigDecimal value = costCalc.calculateTotalCost(technology, order, hashMap);
 // then
 assertEquals(value, BigDecimal.valueOf(org.mockito.Mockito.anyInt()));

 }
 //
 // @Test
 // public void shouldGetValueFromFields() throws Exception {
 // // given
 // hashMap = new HashMap<String, Object>();
 // viewDefinitionState = new V
 // // when
 // hashMap = costCalc.getValueFromFields(viewDefinitionState, state, new String[] { "false" });
 //
 // // then
 // assertEquals(hashMap.size(), new BigDecimal(7));
 // verify(hashMap, times(7)).put(Mockito.anyString(), Mockito.any());
 //
 // }

 }*/
