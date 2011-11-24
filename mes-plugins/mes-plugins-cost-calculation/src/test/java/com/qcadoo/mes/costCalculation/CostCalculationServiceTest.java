/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
