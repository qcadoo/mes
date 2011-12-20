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
package com.qcadoo.mes.technologies.print;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.EntityListImpl;
import com.qcadoo.model.internal.EntityTreeImpl;
import com.qcadoo.model.internal.api.InternalDataDefinition;
import com.qcadoo.report.api.Pair;

public class ReportDataServiceTest {

    private ReportDataService reportDataService = null;

    private final InternalDataDefinition dataDefinition = mock(InternalDataDefinition.class, RETURNS_DEEP_STUBS);

    private final List<Entity> entityTreeList = new ArrayList<Entity>();

    private final List<Entity> entityTreeListWithoutTechnology = new ArrayList<Entity>();

    private final List<Entity> entityTreeListSubTechnologyWithoutRoot = new ArrayList<Entity>();

    private final List<Entity> entityTreeListTechnologyWithoutRoot = new ArrayList<Entity>();

    private EntityTree entityTree = null;

    private final List<Entity> products1 = new ArrayList<Entity>();

    private final List<Entity> products2 = new ArrayList<Entity>();

    private final Entity product3 = new DefaultEntity(dataDefinition);

    private final Entity product2 = new DefaultEntity(dataDefinition);

    private final Entity product1 = new DefaultEntity(dataDefinition);

    private final Entity technologyForQuantityPerTechnologyAlghorithm = new DefaultEntity(dataDefinition);

    private final Entity technologyForQuantityPerOutProductsAlghorithm = new DefaultEntity(dataDefinition);

    private final List<Entity> products4 = new ArrayList<Entity>();

    private final Entity product4 = new DefaultEntity(dataDefinition);

    private final List<Entity> products5 = new ArrayList<Entity>();

    private final List<Entity> products6 = new ArrayList<Entity>();

    private final List<Entity> products7 = new ArrayList<Entity>();

    private final List<Entity> products8 = new ArrayList<Entity>();

    private final List<Entity> products9 = new ArrayList<Entity>();

    private final Entity product5 = new DefaultEntity(dataDefinition);

    private final Entity product6 = new DefaultEntity(dataDefinition);

    private final Entity product7 = new DefaultEntity(dataDefinition);

    private final Entity workPlan = new DefaultEntity(dataDefinition);

    private final List<Entity> components = new ArrayList<Entity>();

    private final List<Entity> componentsOutProducts = new ArrayList<Entity>();

    private final List<Entity> componentsOutProducts2 = new ArrayList<Entity>();

    private final FieldDefinition fieldDefinition = mock(FieldDefinition.class);

    private final FieldDefinition fieldDefinition1 = mock(FieldDefinition.class);

    private final FieldDefinition fieldDefinition2 = mock(FieldDefinition.class);

    private final FieldDefinition fieldDefinition3 = mock(FieldDefinition.class);

    @Before
    public void init() {
        reportDataService = new ReportDataService();

        given(fieldDefinition.getName()).willReturn("technology");
        given(dataDefinition.getField("technology")).willReturn(fieldDefinition);

        given(fieldDefinition1.getName()).willReturn("operationProductInComponent");
        given(dataDefinition.getField("operationProductInComponent")).willReturn(fieldDefinition1);

        given(fieldDefinition2.getName()).willReturn("operationProductOutComponent");
        given(dataDefinition.getField("operationProductOutComponent")).willReturn(fieldDefinition2);

        given(fieldDefinition3.getName()).willReturn("workPlan");
        given(dataDefinition.getField("workPlan")).willReturn(fieldDefinition3);

        entityTree = new EntityTreeImpl(dataDefinition, "technology", new Long(1));

        EntityTree entityTreeSubTechnology = new EntityTreeImpl(dataDefinition, "technology", new Long(2));
        EntityList componentsList1 = new EntityListImpl(dataDefinition, "operationProductInComponent", new Long(1));
        EntityList componentsList2 = new EntityListImpl(dataDefinition, "operationProductInComponent", new Long(2));
        EntityList componentsList3 = new EntityListImpl(dataDefinition, "operationProductInComponent", new Long(3));
        EntityList componentsOutList1 = new EntityListImpl(dataDefinition, "operationProductOutComponent", new Long(11));
        EntityList componentsOutList2 = new EntityListImpl(dataDefinition, "operationProductOutComponent", new Long(12));
        EntityList componentsOutList3 = new EntityListImpl(dataDefinition, "operationProductOutComponent", new Long(13));

        Entity machine1 = new DefaultEntity(dataDefinition);
        machine1.setId(new Long(1));
        Entity machine2 = new DefaultEntity(dataDefinition);
        machine2.setId(new Long(2));

        Entity worker1 = new DefaultEntity(dataDefinition);
        worker1.setId(new Long(1));
        Entity worker2 = new DefaultEntity(dataDefinition);
        worker2.setId(new Long(2));

        Entity operation1 = new DefaultEntity(dataDefinition);
        operation1.setField("workstationType", machine1);
        Entity operationComponentRoot = new DefaultEntity(dataDefinition);
        operationComponentRoot.setField("entityType", "operation");
        operationComponentRoot.setField("parent", null);
        operationComponentRoot.setId(new Long(1));
        operationComponentRoot.setField("operation", operation1);
        operationComponentRoot.setField("operationProductInComponents", componentsList1);
        operationComponentRoot.setField("operationProductOutComponents", componentsOutList1);

        Entity operation2 = new DefaultEntity(dataDefinition);
        operation2.setField("workstationType", machine2);
        Entity operationComponent2 = new DefaultEntity(dataDefinition);
        operationComponent2.setField("entityType", "operation");
        operationComponent2.setField("parent", operationComponentRoot);
        operationComponent2.setId(new Long(2));
        operationComponent2.setField("operation", operation2);
        operationComponent2.setField("operationProductInComponents", componentsList2);
        operationComponent2.setField("operationProductOutComponents", componentsOutList2);

        Entity operation3 = new DefaultEntity(dataDefinition);
        operation3.setField("workstationType", null);
        Entity operationComponent3 = new DefaultEntity(dataDefinition);
        operationComponent3.setField("entityType", "operation");
        operationComponent3.setField("parent", operationComponentRoot);
        operationComponent3.setId(new Long(3));
        operationComponent3.setField("operation", operation3);
        operationComponent3.setField("operationProductInComponents", componentsList3);
        operationComponent3.setField("operationProductOutComponents", componentsOutList3);
        entityTreeList.add(operationComponentRoot);
        entityTreeList.add(operationComponent2);
        entityTreeList.add(operationComponent3);

        Entity technology = new DefaultEntity(dataDefinition);
        technology.setField("operationComponents", entityTreeSubTechnology);
        technology.setField("componentQuantityAlgorithm", "02perTechnology");
        Entity operationTechnology = new DefaultEntity(dataDefinition);
        operationTechnology.setField("entityType", "technology");
        operationTechnology.setField("parent", operationComponentRoot);
        operationTechnology.setId(new Long(4));
        operationTechnology.setField("referenceTechnology", technology);

        Entity operationTechnology2 = new DefaultEntity(dataDefinition);
        operationTechnology2.setField("entityType", "technology");
        operationTechnology2.setField("parent", operationComponentRoot);
        operationTechnology2.setId(new Long(5));
        operationTechnology2.setField("referenceTechnology", technology);

        Entity operationTechnology3 = new DefaultEntity(dataDefinition);
        operationTechnology3.setField("entityType", "technology");
        operationTechnology3.setField("parent", null);
        operationTechnology3.setId(new Long(6));
        operationTechnology3.setField("referenceTechnology", technology);

        entityTreeListWithoutTechnology.addAll(entityTreeList);
        entityTreeListSubTechnologyWithoutRoot.addAll(entityTreeList);
        entityTreeListSubTechnologyWithoutRoot.add(operationTechnology2);
        entityTreeList.add(operationTechnology);
        entityTreeListTechnologyWithoutRoot.add(operationTechnology3);

        technologyForQuantityPerTechnologyAlghorithm.setField("operationComponents", entityTree);
        technologyForQuantityPerTechnologyAlghorithm.setField("componentQuantityAlgorithm", "02perTechnology");

        product1.setField("typeOfMaterial", "02intermediate");
        product1.setField("number", "1");
        product1.setId(new Long(1));
        Entity productInComponent1 = new DefaultEntity(dataDefinition);
        productInComponent1.setField("product", product1);
        productInComponent1.setField("quantity", BigDecimal.ONE);

        product2.setField("typeOfMaterial", "01component");
        product2.setField("number", "2");
        product2.setId(new Long(2));
        Entity productInComponent2 = new DefaultEntity(dataDefinition);
        productInComponent2.setField("product", product2);
        productInComponent2.setField("quantity", new BigDecimal("2"));

        product3.setField("typeOfMaterial", "02intermediate");
        product3.setField("number", "3");
        product3.setId(new Long(3));
        Entity productInComponent3 = new DefaultEntity(dataDefinition);
        productInComponent3.setField("product", product3);
        productInComponent3.setField("quantity", new BigDecimal("3"));

        products1.add(productInComponent1);
        products2.add(productInComponent2);
        products2.add(productInComponent3);

        technologyForQuantityPerOutProductsAlghorithm.setField("operationComponents", entityTree);
        technologyForQuantityPerOutProductsAlghorithm.setField("componentQuantityAlgorithm", "01perProductOut");

        product4.setField("typeOfMaterial", "02intermediate");
        product4.setField("number", "4");
        product4.setId(new Long(4));
        Entity productInComponent4 = new DefaultEntity(dataDefinition);
        productInComponent4.setField("product", product4);
        productInComponent4.setField("quantity", new BigDecimal("5"));
        products4.add(productInComponent4);

        product5.setField("typeOfMaterial", "02intermediate");
        product5.setField("number", "5");
        product5.setId(new Long(5));
        Entity productInComponent5 = new DefaultEntity(dataDefinition);
        productInComponent5.setField("product", product5);
        productInComponent5.setField("quantity", new BigDecimal("3"));
        products5.add(productInComponent5);

        product6.setField("typeOfMaterial", "02intermediate");
        product6.setField("number", "6");
        product6.setId(new Long(6));
        Entity productInComponent6 = new DefaultEntity(dataDefinition);
        productInComponent6.setField("product", product6);
        productInComponent6.setField("quantity", new BigDecimal("2"));

        product7.setField("typeOfMaterial", "04waste");
        product7.setField("number", "7");
        product7.setId(new Long(7));
        Entity productInComponent7 = new DefaultEntity(dataDefinition);
        productInComponent7.setField("product", product7);
        productInComponent7.setField("quantity", new BigDecimal("2"));

        products6.add(productInComponent6);
        products7.add(productInComponent2);
        products7.add(productInComponent7);
        products8.add(productInComponent3);
        products9.add(productInComponent2);
        products9.add(productInComponent3);
        products9.add(productInComponent4);

        workPlan.setId(new Long(15));
        Entity workPlanComponent = new DefaultEntity(dataDefinition);
        Entity order = new DefaultEntity(dataDefinition);
        order.setField("technology", technologyForQuantityPerTechnologyAlghorithm);
        order.setField("plannedQuantity", new BigDecimal("5"));
        workPlanComponent.setField("order", order);
        EntityList componentsList = new EntityListImpl(dataDefinition, "workPlan", new Long(15));
        workPlan.setField("orders", componentsList);
        components.add(workPlanComponent);

        Entity workPlanComponent2 = new DefaultEntity(dataDefinition);
        Entity order2 = new DefaultEntity(dataDefinition);
        order2.setField("technology", technologyForQuantityPerOutProductsAlghorithm);
        order2.setField("plannedQuantity", new BigDecimal("5"));
        workPlanComponent2.setField("order", order2);
        componentsOutProducts.add(workPlanComponent2);

        componentsOutProducts2.add(workPlanComponent2);
        componentsOutProducts2.add(workPlanComponent2);
    }

    @Test
    public void shouldReturnEmptyMapIfTechnologyIsEmptyForWorkPlan() {
        // given
        Entity workPlan = new DefaultEntity(dataDefinition);
        Entity workPlanComponent = new DefaultEntity(dataDefinition);
        Entity order = new DefaultEntity(dataDefinition);
        order.setField("technology", null);
        workPlanComponent.setField("order", order);
        EntityList componentsList = new EntityListImpl(dataDefinition, "workPlan", new Long(1));
        workPlan.setField("orders", componentsList);
        List<Entity> components = new ArrayList<Entity>();
        components.add(workPlanComponent);

        given(dataDefinition.find().list().getEntities()).willReturn(components);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 0);
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmForWorkPlanCauseEmptyProductsIn() {
        // given
        given(dataDefinition.find().belongsTo("orders", new Long(15)).list().getEntities()).willReturn(componentsOutProducts);
        given(dataDefinition.find().belongsTo("technology", new Long(1)).orderAscBy("priority").list().getEntities()).willReturn(
                entityTreeListWithoutTechnology);

        given(dataDefinition.find().belongsTo("operationProductInComponent", new Long(1)).list().getEntities()).willReturn(
                new ArrayList<Entity>());

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 0);
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmForWorkPlanCauseTooManyProductsOut() {
        // given
        given(dataDefinition.find().belongsTo("orders", new Long(15)).list().getEntities()).willReturn(componentsOutProducts);

        given(dataDefinition.find().belongsTo("technology", new Long(1)).orderAscBy("priority").list().getEntities()).willReturn(
                entityTreeListWithoutTechnology);

        given(dataDefinition.find().belongsTo("operationProductInComponent", new Long(1)).list().getEntities()).willReturn(
                products1);

        given(dataDefinition.find().belongsTo("operationProductOutComponent", new Long(11)).list().getEntities()).willReturn(
                products2);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 0);
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmForWorkPlanWithPreviousOperationWithoutProductsIn() {
        // given
        given(dataDefinition.find().belongsTo("orders", new Long(15)).list().getEntities()).willReturn(componentsOutProducts);
        given(dataDefinition.find().belongsTo("technology", new Long(1)).orderAscBy("priority").list().getEntities()).willReturn(
                entityTreeListSubTechnologyWithoutRoot);

        given(dataDefinition.find().belongsTo("operationProductInComponent", new Long(1)).list().getEntities()).willReturn(
                products9);

        given(dataDefinition.find().belongsTo("operationProductInComponent", new Long(2)).list().getEntities()).willReturn(
                products5);

        given(dataDefinition.find().belongsTo("operationProductInComponent", new Long(3)).list().getEntities()).willReturn(
                new ArrayList<Entity>());

        given(dataDefinition.find().belongsTo("operationProductOutComponent", new Long(11)).list().getEntities()).willReturn(
                products4);

        given(dataDefinition.find().belongsTo("operationProductOutComponent", new Long(12)).list().getEntities()).willReturn(
                products7);

        given(dataDefinition.find().belongsTo("operationProductOutComponent", new Long(13)).list().getEntities()).willReturn(
                products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 0);
    }
}
