/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.1
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
package com.qcadoo.mes.materialRequirements;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.materialRequirements.print.MaterialRequirementReportDataService;
import com.qcadoo.mes.technologies.print.ReportDataService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.EntityListImpl;
import com.qcadoo.model.internal.EntityTreeImpl;

@Ignore
// TODO masz fix tests
public class MaterialRequirementReportDataServiceTest {

    private MaterialRequirementReportDataService materialRequirementsReportDataService = null;

    private final ReportDataService reportDataService = mock(ReportDataService.class);

    private final DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

    private final Entity technologyForQuantityPerTechnologyAlghorithm = new DefaultEntity(dataDefinition);

    private EntityTree entityTree = null;

    private final List<Entity> products1 = new ArrayList<Entity>();

    private final List<Entity> products2 = new ArrayList<Entity>();

    private final List<Entity> products3 = new ArrayList<Entity>();

    private final Entity product3 = new DefaultEntity(dataDefinition);

    private final Entity product2 = new DefaultEntity(dataDefinition);

    private final Entity product1 = new DefaultEntity(dataDefinition);

    private final List<Entity> entityTreeList = new ArrayList<Entity>();

    private final List<Entity> entityTreeListWithoutTechnology = new ArrayList<Entity>();

    @Before
    public void init() {
        materialRequirementsReportDataService = new MaterialRequirementReportDataService();
        ReflectionTestUtils.setField(materialRequirementsReportDataService, "reportDataService", reportDataService);

        FieldDefinition operationProductInCompoentFieldDefinition = mock(FieldDefinition.class);
        given(operationProductInCompoentFieldDefinition.getName()).willReturn("operationProductInComponent");
        given(dataDefinition.getField("operationProductInComponent")).willReturn(operationProductInCompoentFieldDefinition);

        FieldDefinition operationProductOutCompoentFieldDefinition = mock(FieldDefinition.class);
        given(operationProductOutCompoentFieldDefinition.getName()).willReturn("operationProductOutComponent");
        given(dataDefinition.getField("operationProductOutComponent")).willReturn(operationProductOutCompoentFieldDefinition);

        FieldDefinition technologyFieldDefinition = mock(FieldDefinition.class);
        given(technologyFieldDefinition.getName()).willReturn("technology");
        given(dataDefinition.getField("technology")).willReturn(technologyFieldDefinition);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getName()).willReturn("joinField");
        given(dataDefinition.getField("joinField")).willReturn(fieldDefinition);

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

        entityTreeListWithoutTechnology.addAll(entityTreeList);
        entityTreeList.add(operationTechnology);

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

        given(dataDefinition.find().list().getEntities()).willReturn(components);

        // when
        Map<Entity, BigDecimal> products = materialRequirementsReportDataService.prepareTechnologySeries(materialRequirement);

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

        given(dataDefinition.find().list().getEntities()).willReturn(components);

        // when
        Map<Entity, BigDecimal> products = materialRequirementsReportDataService.prepareTechnologySeries(materialRequirement);

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

        given(dataDefinition.find().list().getEntities()).willReturn(components);

        // when
        Map<Entity, BigDecimal> products = materialRequirementsReportDataService.prepareTechnologySeries(materialRequirement);

        // then
        assertEquals(products.size(), 0);
    }

    @Test
    public void shouldReturnNotEmptyMapIfQuantityIsMoreThenZero() {
        // given
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getName()).willReturn("materialRequirement");
        given(dataDefinition.getField("materialRequirement")).willReturn(fieldDefinition);

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

        given(dataDefinition.find().belongsTo("materialRequirement", new Long(15)).list().getEntities()).willReturn(components);

        given(dataDefinition.find().belongsTo("technology", new Long(1)).orderAscBy("priority").list().getEntities()).willReturn(
                entityTreeList);
        given(dataDefinition.find().belongsTo("technology", new Long(2)).orderAscBy("priority").list().getEntities()).willReturn(
                entityTreeListWithoutTechnology);

        given(dataDefinition.find().belongsTo("operationProductInComponent", new Long(1)).list().getEntities()).willReturn(
                products1);
        given(dataDefinition.find().belongsTo("operationProductInComponent", new Long(2)).list().getEntities()).willReturn(
                products2);
        given(dataDefinition.find().belongsTo("operationProductInComponent", new Long(3)).list().getEntities()).willReturn(
                products3);

        // when
        Map<Entity, BigDecimal> products = materialRequirementsReportDataService.prepareTechnologySeries(materialRequirement);

        // then
        assertEquals(products.size(), 3);
    }

}
