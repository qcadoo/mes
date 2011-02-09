package com.qcadoo.mes.products;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.EntityList;
import com.qcadoo.mes.internal.EntityTree;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.Restriction;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.products.print.ReportDataService;

public class ReportDataServiceTest {

    private ReportDataService reportDataService = mock(ReportDataService.class);

    private DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

    private List<Entity> entityTreeList = new ArrayList<Entity>();

    private List<Entity> entityTreeListWithoutTechnology = new ArrayList<Entity>();

    private EntityTree entityTree = new EntityTree(dataDefinition, "technology", new Long(1));

    @Before
    public void init() {
        EntityTree entityTreeSubTechnology = new EntityTree(dataDefinition, "technology", new Long(2));
        Entity operationRoot = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        operationRoot.setField("entityType", "operation");
        operationRoot.setField("parent", null);
        operationRoot.setId(new Long(1));
        Entity operation2 = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        operation2.setField("entityType", "operation");
        operation2.setField("parent", operationRoot);
        operation2.setId(new Long(2));
        Entity operation3 = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        operation3.setField("entityType", "operation");
        operation3.setField("parent", operationRoot);
        operation3.setId(new Long(3));
        entityTreeList.add(operationRoot);
        entityTreeList.add(operation2);
        entityTreeList.add(operation3);
        Entity technology = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        technology.setField("operationComponents", entityTreeSubTechnology);
        Entity operationTechnology = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        operationTechnology.setField("entityType", "technology");
        operationTechnology.setField("parent", operationRoot);
        operationTechnology.setId(new Long(4));
        operationTechnology.setField("referenceTechnology", technology);
        entityTreeListWithoutTechnology.addAll(entityTreeList);
        entityTreeList.add(operationTechnology);
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
    public void shouldReturnEmptyListIfTechnologyIsEmpty() {
        // given
        Entity materialRequirement = new DefaultEntity("products", "materialRequirement");
        Entity materialRequirementComponent = new DefaultEntity("products", "materialRequirementComponent");
        Entity order = new DefaultEntity("products", "order");
        order.setField("technology", null);
        materialRequirementComponent.setField("order", order);
        EntityList componentsList = new EntityList(dataDefinition, "materialRequirement", new Long(1));
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
    public void shouldReturnEmptyListIfQuantityIsEmpty() {
        // given
        Entity materialRequirement = new DefaultEntity("products", "materialRequirement");
        Entity materialRequirementComponent = new DefaultEntity("products", "materialRequirementComponent");
        Entity order = new DefaultEntity("products", "order");
        order.setField("quantity", null);
        materialRequirementComponent.setField("order", order);
        EntityList componentsList = new EntityList(dataDefinition, "materialRequirement", new Long(1));
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
    public void shouldReturnEmptyListIfQuantityIsLessThenOne() {
        // given
        Entity materialRequirement = new DefaultEntity("products", "materialRequirement");
        Entity materialRequirementComponent = new DefaultEntity("products", "materialRequirementComponent");
        Entity order = new DefaultEntity("products", "order");
        order.setField("quantity", new BigDecimal(0));
        materialRequirementComponent.setField("order", order);
        EntityList componentsList = new EntityList(dataDefinition, "materialRequirement", new Long(1));
        materialRequirement.setField("orders", componentsList);
        List<Entity> components = new ArrayList<Entity>();
        components.add(materialRequirementComponent);

        given(dataDefinition.find().restrictedWith(any(Restriction.class)).list().getEntities()).willReturn(components);

        // when
        Map<Entity, BigDecimal> products = reportDataService.prepareTechnologySeries(materialRequirement);

        // then
        assertEquals(products.size(), 0);
    }

}
