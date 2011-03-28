/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.products;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.products.print.ReportDataService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.Restriction;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.EntityListImpl;
import com.qcadoo.model.internal.EntityTreeImpl;
import com.qcadoo.report.api.Pair;

public class ReportDataServiceTest {

    private ReportDataService reportDataService = null;

    private final DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

    private final List<Entity> entityTreeList = new ArrayList<Entity>();

    private final List<Entity> entityTreeListWithoutTechnology = new ArrayList<Entity>();

    private final List<Entity> entityTreeListSubTechnologyWithoutRoot = new ArrayList<Entity>();

    private final List<Entity> entityTreeListTechnologyWithoutRoot = new ArrayList<Entity>();

    private final EntityTree entityTree = new EntityTreeImpl(dataDefinition, "technology", new Long(1));

    private final List<Entity> products1 = new ArrayList<Entity>();

    private final List<Entity> products2 = new ArrayList<Entity>();

    private final List<Entity> products3 = new ArrayList<Entity>();

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

    @Before
    public void init() {
        reportDataService = new ReportDataService();

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
        operation1.setField("machine", machine1);
        operation1.setField("staff", null);
        Entity operationComponentRoot = new DefaultEntity(dataDefinition);
        operationComponentRoot.setField("entityType", "operation");
        operationComponentRoot.setField("parent", null);
        operationComponentRoot.setId(new Long(1));
        operationComponentRoot.setField("operation", operation1);
        operationComponentRoot.setField("operationProductInComponents", componentsList1);
        operationComponentRoot.setField("operationProductOutComponents", componentsOutList1);

        Entity operation2 = new DefaultEntity(dataDefinition);
        operation2.setField("machine", machine2);
        operation2.setField("staff", worker2);
        Entity operationComponent2 = new DefaultEntity(dataDefinition);
        operationComponent2.setField("entityType", "operation");
        operationComponent2.setField("parent", operationComponentRoot);
        operationComponent2.setId(new Long(2));
        operationComponent2.setField("operation", operation2);
        operationComponent2.setField("operationProductInComponents", componentsList2);
        operationComponent2.setField("operationProductOutComponents", componentsOutList2);

        Entity operation3 = new DefaultEntity(dataDefinition);
        operation3.setField("machine", null);
        operation3.setField("staff", worker1);
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
    public void shouldReturnValidList() {
        // given
        List<Entity> operationComponents = new ArrayList<Entity>();

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        // when
        reportDataService.addOperationsFromSubtechnologiesToList(entityTree, operationComponents);

        // then
        assertEquals(operationComponents.size(), 6);
    }

    @Test
    public void shouldReturnEmptyMapIfTechnologyIsEmpty() {
        // given
        Entity materialRequirement = new DefaultEntity(dataDefinition);
        Entity materialRequirementComponent = new DefaultEntity(dataDefinition);
        Entity order = new DefaultEntity(dataDefinition);
        order.setField("technology", null);
        materialRequirementComponent.setField("order", order);
        EntityList componentsList = new EntityListImpl(dataDefinition, "materialRequirement", new Long(1));
        materialRequirement.setField("orders", componentsList);
        List<Entity> components = new ArrayList<Entity>();
        components.add(materialRequirementComponent);

        given(dataDefinition.find().restrictedWith(any(Restriction.class)).list().getEntities()).willReturn(components);

        // when
        Map<Entity, BigDecimal> products = reportDataService.prepareTechnologySeries(materialRequirement);

        // then
        assertEquals(products.size(), 0);
    }

    @Test
    public void shouldReturnEmptyMapIfQuantityIsEmpty() {
        // given
        Entity materialRequirement = new DefaultEntity(dataDefinition);
        Entity materialRequirementComponent = new DefaultEntity(dataDefinition);
        Entity order = new DefaultEntity(dataDefinition);
        order.setField("technology", new DefaultEntity(dataDefinition));
        order.setField("plannedQuantity", null);
        materialRequirementComponent.setField("order", order);
        EntityList componentsList = new EntityListImpl(dataDefinition, "materialRequirement", new Long(1));
        materialRequirement.setField("orders", componentsList);
        List<Entity> components = new ArrayList<Entity>();
        components.add(materialRequirementComponent);

        given(dataDefinition.find().restrictedWith(any(Restriction.class)).list().getEntities()).willReturn(components);

        // when
        Map<Entity, BigDecimal> products = reportDataService.prepareTechnologySeries(materialRequirement);

        // then
        assertEquals(products.size(), 0);
    }

    @Test
    public void shouldReturnEmptyMapIfQuantityIsLessThenOne() {
        // given
        Entity materialRequirement = new DefaultEntity(dataDefinition);
        Entity materialRequirementComponent = new DefaultEntity(dataDefinition);
        Entity order = new DefaultEntity(dataDefinition);
        order.setField("technology", new DefaultEntity(dataDefinition));
        order.setField("plannedQuantity", BigDecimal.ZERO);
        materialRequirementComponent.setField("order", order);
        EntityList componentsList = new EntityListImpl(dataDefinition, "materialRequirement", new Long(1));
        materialRequirement.setField("orders", componentsList);
        List<Entity> components = new ArrayList<Entity>();
        components.add(materialRequirementComponent);

        given(dataDefinition.find().restrictedWith(any(Restriction.class)).list().getEntities()).willReturn(components);

        // when
        Map<Entity, BigDecimal> products = reportDataService.prepareTechnologySeries(materialRequirement);

        // then
        assertEquals(products.size(), 0);
    }

    @Test
    public void shouldReturnNotEmptyMapIfQuantityIsMoreThenZero() {
        // given
        Entity materialRequirement = new DefaultEntity(dataDefinition);
        materialRequirement.setField("onlyComponents", false);
        materialRequirement.setId(new Long(15));
        Entity materialRequirementComponent = new DefaultEntity(dataDefinition);
        Entity order = new DefaultEntity(dataDefinition);
        order.setField("technology", technologyForQuantityPerTechnologyAlghorithm);
        order.setField("plannedQuantity", BigDecimal.ONE);
        materialRequirementComponent.setField("order", order);
        EntityList componentsList = new EntityListImpl(dataDefinition, "materialRequirement", new Long(15));
        materialRequirement.setField("orders", componentsList);
        List<Entity> components = new ArrayList<Entity>();
        components.add(materialRequirementComponent);

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(components);

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products2);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products3);

        // when
        Map<Entity, BigDecimal> products = reportDataService.prepareTechnologySeries(materialRequirement);

        // then
        assertEquals(products.size(), 3);
    }

    @Test
    public void shouldReturnValidMapPerTechnologyAlgorithm() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products2);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products3);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerTechnologyAlghorithm, new BigDecimal(2),
                false);

        // then
        assertEquals(products.size(), 3);
        assertEquals(products.get(product1), new BigDecimal("4"));
        assertEquals(products.get(product2), new BigDecimal("8"));
        assertEquals(products.get(product3), new BigDecimal("12"));
    }

    @Test
    public void shouldReturnValidMapPerTechnologyAlgorithmForComponentsOnly() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products2);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products3);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerTechnologyAlghorithm, new BigDecimal(2),
                true);

        // then
        assertEquals(products.size(), 1);
        assertEquals(products.get(product2), new BigDecimal("8"));

    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmCauseEmptyRoot() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(new ArrayList<Entity>());

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(2),
                false);

        // then
        assertEquals(products.size(), 0);
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmCauseEmptyProductsIn() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(new ArrayList<Entity>());

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(2),
                false);

        // then
        assertEquals(products.size(), 0);
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmCauseEmptyProductsOut() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products3);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(2),
                false);

        // then
        assertEquals(products.size(), 0);
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmCauseTooManyProductsOut() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products2);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(2),
                false);

        // then
        assertEquals(products.size(), 0);
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmWithoutPreviousOperation() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products4);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(5),
                false);

        // then
        assertEquals(products.size(), 1);
        assertEquals(products.get(product1), new BigDecimal("1"));

    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmWithPreviousOperation() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products2);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(5),
                false);

        // then
        assertEquals(products.size(), 4);
        assertEquals(products.get(product2), new BigDecimal("2"));
        assertEquals(products.get(product3), new BigDecimal("3"));
        assertEquals(products.get(product5), new BigDecimal("3"));
        assertEquals(products.get(product6), new BigDecimal("2"));
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmWithPreviousOperationComponentRepeat() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products2);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(5),
                false);

        // then
        assertEquals(products.size(), 4);
        assertEquals(products.get(product2), new BigDecimal("4"));
        assertEquals(products.get(product3), new BigDecimal("3"));
        assertEquals(products.get(product5), new BigDecimal("3"));
        assertEquals(products.get(product7), new BigDecimal("2"));
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmWithAddResults() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        products.put(product2, new BigDecimal("7"));
        products.put(product1, new BigDecimal("6"));
        products.put(product3, new BigDecimal("2"));
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products2);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(5),
                false);

        // then
        assertEquals(products.size(), 5);
        assertEquals(products.get(product1), new BigDecimal("6"));
        assertEquals(products.get(product2), new BigDecimal("9"));
        assertEquals(products.get(product3), new BigDecimal("5"));
        assertEquals(products.get(product5), new BigDecimal("3"));
        assertEquals(products.get(product6), new BigDecimal("2"));
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmWithSubtechnology() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(5),
                false);

        // then
        assertEquals(products.size(), 5);
        assertEquals(products.get(product2), new BigDecimal("4"));
        assertEquals(products.get(product3), new BigDecimal("6"));
        assertEquals(products.get(product4), BigDecimal.TEN);
        assertEquals(products.get(product5), new BigDecimal("6"));
        assertEquals(products.get(product6), new BigDecimal("4"));
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmWithSubtechnologyWithoutRoot() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListSubTechnologyWithoutRoot);

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(new ArrayList<Entity>());

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(5),
                false);

        // then
        assertEquals(products.size(), 5);
        assertEquals(products.get(product2), new BigDecimal("2"));
        assertEquals(products.get(product3), new BigDecimal("3"));
        assertEquals(products.get(product4), new BigDecimal("5"));
        assertEquals(products.get(product5), new BigDecimal("3"));
        assertEquals(products.get(product6), new BigDecimal("2"));
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmWithTechnologyWithoutRoot() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListTechnologyWithoutRoot);

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(new ArrayList<Entity>());

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(5),
                false);

        // then
        assertEquals(products.size(), 0);

    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmWithPreviousOperationWithoutProductsIn() {
        // given
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products2);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(new ArrayList<Entity>());

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(5),
                false);

        // then
        assertEquals(products.size(), 0);
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

        given(dataDefinition.find().restrictedWith(any(Restriction.class)).list().getEntities()).willReturn(components);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 0);
    }

    @Test
    public void shouldReturnNotEmptyMapIfTechnologyIsNotEmptyForWorkPlan() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(components);

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products2);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products3);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 1);
    }

    @Test
    public void shouldReturnValidMapPerTechnologyAlgorithmForWorkPlanPerProduct() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(components);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 1);
        Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operComponents = operations.get(null);
        assertEquals(operComponents.size(), 3);
        for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entry : operComponents
                .entrySet()) {
            Map<Entity, BigDecimal> productsIn = entry.getValue().getKey();
            Map<Entity, BigDecimal> productsOut = entry.getValue().getValue();
            if (entry.getKey().getKey().getId().equals(new Long(1))) {
                assertEquals(productsIn.get(product2), new BigDecimal("10"));
                assertEquals(productsIn.get(product3), new BigDecimal("15"));
                assertEquals(productsIn.get(product4), new BigDecimal("25"));
                assertEquals(productsOut.get(product4), new BigDecimal("25"));
            } else if (entry.getKey().getKey().getId().equals(new Long(2))) {
                assertEquals(productsIn.get(product5), new BigDecimal("15"));
                assertEquals(productsOut.get(product2), new BigDecimal("10"));
            } else if (entry.getKey().getKey().getId().equals(new Long(3))) {
                assertEquals(productsIn.get(product6), new BigDecimal("10"));
                assertEquals(productsOut.get(product3), new BigDecimal("15"));
            }
        }
    }

    @Test
    public void shouldReturnValidMapPerTechnologyAlgorithmForWorkPlanPerMachine() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(components);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "machine");

        // then
        assertEquals(operations.size(), 3);
        for (Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operation : operations
                .entrySet()) {
            for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entry : operation.getValue()
                    .entrySet()) {
                Map<Entity, BigDecimal> productsIn = entry.getValue().getKey();
                Map<Entity, BigDecimal> productsOut = entry.getValue().getValue();
                if (entry.getKey().getKey().getId().equals(new Long(1)) && operation.getKey().getId().equals(new Long(1))) {
                    assertEquals(productsIn.get(product2), new BigDecimal("10"));
                    assertEquals(productsIn.get(product3), new BigDecimal("15"));
                    assertEquals(productsIn.get(product4), new BigDecimal("25"));
                    assertEquals(productsOut.get(product4), new BigDecimal("25"));
                } else if (entry.getKey().getKey().getId().equals(new Long(2)) && operation.getKey().getId().equals(new Long(2))) {
                    assertEquals(productsIn.get(product5), new BigDecimal("15"));
                    assertEquals(productsOut.get(product2), new BigDecimal("10"));
                } else if (entry.getKey().getKey().getId().equals(new Long(3)) && operation.getKey() == null) {
                    assertEquals(productsIn.get(product6), new BigDecimal("10"));
                    assertEquals(productsOut.get(product3), new BigDecimal("15"));
                }
            }
        }
    }

    @Test
    public void shouldReturnValidMapPerTechnologyAlgorithmForWorkPlanPerWorker() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(components);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "worker");

        // then
        assertEquals(operations.size(), 3);
        for (Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operation : operations
                .entrySet()) {
            for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entry : operation.getValue()
                    .entrySet()) {
                Map<Entity, BigDecimal> productsIn = entry.getValue().getKey();
                Map<Entity, BigDecimal> productsOut = entry.getValue().getValue();
                if (entry.getKey().getKey().getId().equals(new Long(1)) && operation.getKey() == null) {
                    assertEquals(productsIn.get(product2), new BigDecimal("10"));
                    assertEquals(productsIn.get(product3), new BigDecimal("15"));
                    assertEquals(productsIn.get(product4), new BigDecimal("25"));
                    assertEquals(productsOut.get(product4), new BigDecimal("25"));
                } else if (entry.getKey().getKey().getId().equals(new Long(2)) && operation.getKey().getId().equals(new Long(2))) {
                    assertEquals(productsIn.get(product5), new BigDecimal("15"));
                    assertEquals(productsOut.get(product2), new BigDecimal("10"));
                } else if (entry.getKey().getKey().getId().equals(new Long(3)) && operation.getKey().getId().equals(new Long(1))) {
                    assertEquals(productsIn.get(product6), new BigDecimal("10"));
                    assertEquals(productsOut.get(product3), new BigDecimal("15"));
                }
            }
        }
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmForWorkPlanPerProduct() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(componentsOutProducts);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 1);
        Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operComponents = operations.get(null);
        assertEquals(operComponents.size(), 6);
        for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entry : operComponents
                .entrySet()) {
            Map<Entity, BigDecimal> productsIn = entry.getValue().getKey();
            Map<Entity, BigDecimal> productsOut = entry.getValue().getValue();
            if (entry.getKey().getKey().getId().equals(new Long(1))) {
                assertEquals(productsIn.get(product2), new BigDecimal("2"));
                assertEquals(productsIn.get(product3), new BigDecimal("3"));
                assertEquals(productsIn.get(product4), new BigDecimal("5"));
                assertEquals(productsOut.get(product4), new BigDecimal("5"));
            } else if (entry.getKey().getKey().getId().equals(new Long(2))) {
                assertEquals(productsIn.get(product5), new BigDecimal("3"));
                assertEquals(productsOut.get(product2), new BigDecimal("2"));
            } else if (entry.getKey().getKey().getId().equals(new Long(3))) {
                assertEquals(productsIn.get(product6), new BigDecimal("2"));
                assertEquals(productsOut.get(product3), new BigDecimal("3"));
            }
        }
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmForWorkPlanPerMachine() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(componentsOutProducts);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "machine");

        // then
        assertEquals(operations.size(), 3);
        for (Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operation : operations
                .entrySet()) {
            for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entry : operation.getValue()
                    .entrySet()) {
                Map<Entity, BigDecimal> productsIn = entry.getValue().getKey();
                Map<Entity, BigDecimal> productsOut = entry.getValue().getValue();
                if (entry.getKey().getKey().getId().equals(new Long(1)) && operation.getKey().getId().equals(new Long(1))) {
                    assertEquals(productsIn.get(product2), new BigDecimal("2"));
                    assertEquals(productsIn.get(product3), new BigDecimal("3"));
                    assertEquals(productsIn.get(product4), new BigDecimal("5"));
                    assertEquals(productsOut.get(product4), new BigDecimal("5"));
                } else if (entry.getKey().getKey().getId().equals(new Long(2)) && operation.getKey().getId().equals(new Long(2))) {
                    assertEquals(productsIn.get(product5), new BigDecimal("3"));
                    assertEquals(productsOut.get(product2), new BigDecimal("2"));
                } else if (entry.getKey().getKey().getId().equals(new Long(3)) && operation.getKey() == null) {
                    assertEquals(productsIn.get(product6), new BigDecimal("2"));
                    assertEquals(productsOut.get(product3), new BigDecimal("3"));
                }
            }
        }
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmForWorkPlanPerWorker() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(componentsOutProducts);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "worker");

        // then
        assertEquals(operations.size(), 3);
        for (Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operation : operations
                .entrySet()) {
            for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entry : operation.getValue()
                    .entrySet()) {
                Map<Entity, BigDecimal> productsIn = entry.getValue().getKey();
                Map<Entity, BigDecimal> productsOut = entry.getValue().getValue();
                if (entry.getKey().getKey().getId().equals(new Long(1)) && operation.getKey() == null) {
                    assertEquals(productsIn.get(product2), new BigDecimal("2"));
                    assertEquals(productsIn.get(product3), new BigDecimal("3"));
                    assertEquals(productsIn.get(product4), new BigDecimal("5"));
                    assertEquals(productsOut.get(product4), new BigDecimal("5"));
                } else if (entry.getKey().getKey().getId().equals(new Long(2)) && operation.getKey().getId().equals(new Long(2))) {
                    assertEquals(productsIn.get(product5), new BigDecimal("3"));
                    assertEquals(productsOut.get(product2), new BigDecimal("2"));
                } else if (entry.getKey().getKey().getId().equals(new Long(3)) && operation.getKey().getId().equals(new Long(1))) {
                    assertEquals(productsIn.get(product6), new BigDecimal("2"));
                    assertEquals(productsOut.get(product3), new BigDecimal("3"));
                }
            }
        }
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmForWorkPlanPerProductWithAddResults() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(componentsOutProducts2);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeList);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(2)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products6);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 1);
        Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operComponents = operations.get(null);
        assertEquals(operComponents.size(), 6);
        for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entry : operComponents
                .entrySet()) {
            Map<Entity, BigDecimal> productsIn = entry.getValue().getKey();
            Map<Entity, BigDecimal> productsOut = entry.getValue().getValue();
            if (entry.getKey().getKey().getId().equals(new Long(1))) {
                assertEquals(productsIn.get(product2), new BigDecimal("2"));
                assertEquals(productsIn.get(product3), new BigDecimal("3"));
                assertEquals(productsIn.get(product4), new BigDecimal("5"));
                assertEquals(productsOut.get(product4), new BigDecimal("5"));
            } else if (entry.getKey().getKey().getId().equals(new Long(2))) {
                assertEquals(productsIn.get(product5), new BigDecimal("3"));
                assertEquals(productsOut.get(product2), new BigDecimal("2"));
            } else if (entry.getKey().getKey().getId().equals(new Long(3))) {
                assertEquals(productsIn.get(product6), new BigDecimal("2"));
                assertEquals(productsOut.get(product3), new BigDecimal("3"));
            }
        }
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmForWorkPlanCauseEmptyProductsIn() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(componentsOutProducts);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(new ArrayList<Entity>());

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 0);
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmForWorkPlanCauseTooManyProductsOut() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(componentsOutProducts);

        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products2);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 0);
    }

    @Test
    public void shouldReturnValidMapPerOutProductsAlgorithmForWorkPlanWithoutPreviousOperation() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(componentsOutProducts);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListWithoutTechnology);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products1);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products4);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 1);
        Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operComponents = operations.get(null);
        assertEquals(operComponents.size(), 1);
    }

    @Test
    public void shouldReturnInvalidMapPerOutProductsAlgorithmForWorkPlanWithPreviousOperationWithoutProductsIn() {
        // given
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("orders"), new Long(15)))
                        .list().getEntities()).willReturn(componentsOutProducts);
        given(
                dataDefinition.find().restrictedWith(Restrictions.belongsTo(dataDefinition.getField("technology"), new Long(1)))
                        .orderAscBy("priority").list().getEntities()).willReturn(entityTreeListSubTechnologyWithoutRoot);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products9);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products5);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(new ArrayList<Entity>());

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(11)))
                        .list().getEntities()).willReturn(products4);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(12)))
                        .list().getEntities()).willReturn(products7);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(13)))
                        .list().getEntities()).willReturn(products8);

        // when
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = reportDataService
                .prepareOperationSeries(workPlan, "product");

        // then
        assertEquals(operations.size(), 0);
    }
}
