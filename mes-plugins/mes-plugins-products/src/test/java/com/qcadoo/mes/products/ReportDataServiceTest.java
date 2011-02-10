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

    private ReportDataService reportDataService = null;

    private DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

    private List<Entity> entityTreeList = new ArrayList<Entity>();

    private List<Entity> entityTreeListWithoutTechnology = new ArrayList<Entity>();

    private EntityTree entityTree = new EntityTree(dataDefinition, "technology", new Long(1));

    private List<Entity> products1 = new ArrayList<Entity>();

    private List<Entity> products2 = new ArrayList<Entity>();

    private List<Entity> products3 = new ArrayList<Entity>();

    private Entity product3 = new DefaultEntity("products", "product");

    private Entity product2 = new DefaultEntity("products", "product");

    private Entity product1 = new DefaultEntity("products", "product");

    private Entity technologyForQuantityPerTechnologyAlghorithm = new DefaultEntity("products", "technology");

    private Entity technologyForQuantityPerOutProductsAlghorithm = new DefaultEntity("products", "technology");

    @Before
    public void init() {
        reportDataService = new ReportDataService();
        EntityTree entityTreeSubTechnology = new EntityTree(dataDefinition, "technology", new Long(2));
        EntityList componentsList1 = new EntityList(dataDefinition, "operationProductInComponent", new Long(1));
        EntityList componentsList2 = new EntityList(dataDefinition, "operationProductInComponent", new Long(2));
        EntityList componentsList3 = new EntityList(dataDefinition, "operationProductInComponent", new Long(3));
        EntityList componentsOutList1 = new EntityList(dataDefinition, "operationProductOutComponent", new Long(1));
        EntityList componentsOutList2 = new EntityList(dataDefinition, "operationProductOutComponent", new Long(2));
        EntityList componentsOutList3 = new EntityList(dataDefinition, "operationProductOutComponent", new Long(3));
        Entity operationRoot = new DefaultEntity("products", "technologyOperationComponent");
        operationRoot.setField("entityType", "operation");
        operationRoot.setField("parent", null);
        operationRoot.setId(new Long(1));
        operationRoot.setField("operationProductInComponents", componentsList1);
        operationRoot.setField("operationProductOutComponents", componentsOutList1);
        Entity operation2 = new DefaultEntity("products", "technologyOperationComponent");
        operation2.setField("entityType", "operation");
        operation2.setField("parent", operationRoot);
        operation2.setId(new Long(2));
        operation2.setField("operationProductInComponents", componentsList2);
        operation2.setField("operationProductOutComponents", componentsOutList2);
        Entity operation3 = new DefaultEntity("products", "technologyOperationComponent");
        operation3.setField("entityType", "operation");
        operation3.setField("parent", operationRoot);
        operation3.setId(new Long(3));
        operation3.setField("operationProductInComponents", componentsList3);
        operation3.setField("operationProductOutComponents", componentsOutList3);
        entityTreeList.add(operationRoot);
        entityTreeList.add(operation2);
        entityTreeList.add(operation3);
        Entity technology = new DefaultEntity("products", "technology");
        technology.setField("operationComponents", entityTreeSubTechnology);
        technology.setField("componentQuantityAlgorithm", "02perTechnology");
        Entity operationTechnology = new DefaultEntity("products", "technologyOperationComponent");
        operationTechnology.setField("entityType", "technology");
        operationTechnology.setField("parent", operationRoot);
        operationTechnology.setId(new Long(4));
        operationTechnology.setField("referenceTechnology", technology);
        entityTreeListWithoutTechnology.addAll(entityTreeList);
        entityTreeList.add(operationTechnology);
        technologyForQuantityPerTechnologyAlghorithm.setField("operationComponents", entityTree);
        technologyForQuantityPerTechnologyAlghorithm.setField("componentQuantityAlgorithm", "02perTechnology");
        product1.setField("typeOfMaterial", "02intermediate");
        product1.setId(new Long(1));
        Entity productInComponent1 = new DefaultEntity("products", "operationProductInComponent");
        productInComponent1.setField("product", product1);
        productInComponent1.setField("quantity", BigDecimal.ONE);
        product2.setField("typeOfMaterial", "01component");
        product2.setId(new Long(2));
        Entity productInComponent2 = new DefaultEntity("products", "operationProductInComponent");
        productInComponent2.setField("product", product2);
        productInComponent2.setField("quantity", BigDecimal.ONE);
        product3.setField("typeOfMaterial", "02intermediate");
        product3.setId(new Long(3));
        Entity productInComponent3 = new DefaultEntity("products", "operationProductInComponent");
        productInComponent3.setField("product", product3);
        productInComponent3.setField("quantity", BigDecimal.ONE);
        products1.add(productInComponent1);
        products2.add(productInComponent2);
        products2.add(productInComponent3);

        technologyForQuantityPerOutProductsAlghorithm.setField("operationComponents", entityTree);
        technologyForQuantityPerOutProductsAlghorithm.setField("componentQuantityAlgorithm", "01perProductOut");
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
        order.setField("technology", new DefaultEntity("products", "technology"));
        order.setField("plannedQuantity", null);
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
        order.setField("technology", new DefaultEntity("products", "technology"));
        order.setField("plannedQuantity", BigDecimal.ZERO);
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
    public void shouldReturnEmptyListIfQuantityIsMoreThenZero() {
        // given
        Entity materialRequirement = new DefaultEntity("products", "materialRequirement");
        materialRequirement.setField("onlyComponents", false);
        materialRequirement.setId(new Long(15));
        Entity materialRequirementComponent = new DefaultEntity("products", "materialRequirementComponent");
        Entity order = new DefaultEntity("products", "order");
        order.setField("technology", technologyForQuantityPerTechnologyAlghorithm);
        order.setField("plannedQuantity", BigDecimal.ONE);
        materialRequirementComponent.setField("order", order);
        EntityList componentsList = new EntityList(dataDefinition, "materialRequirement", new Long(15));
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
        assertEquals(products.get(product2), new BigDecimal("4"));
        assertEquals(products.get(product3), new BigDecimal("4"));
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
        assertEquals(products.get(product2), new BigDecimal("4"));

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
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products2);
        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductInComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products3);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(1)))
                        .list().getEntities()).willReturn(products3);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(2)))
                        .list().getEntities()).willReturn(products3);

        given(
                dataDefinition
                        .find()
                        .restrictedWith(
                                Restrictions.belongsTo(dataDefinition.getField("operationProductOutComponents"), new Long(3)))
                        .list().getEntities()).willReturn(products3);

        // when
        reportDataService.countQuantityForProductsIn(products, technologyForQuantityPerOutProductsAlghorithm, new BigDecimal(2),
                false);

        // then
        assertEquals(products.size(), 0);
    }

}
