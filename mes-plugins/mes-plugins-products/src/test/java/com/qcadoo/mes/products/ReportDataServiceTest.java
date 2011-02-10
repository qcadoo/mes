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

    private List<Entity> entityTreeListSubTechnologyWithoutRoot = new ArrayList<Entity>();

    private List<Entity> entityTreeListTechnologyWithoutRoot = new ArrayList<Entity>();

    private EntityTree entityTree = new EntityTree(dataDefinition, "technology", new Long(1));

    private List<Entity> products1 = new ArrayList<Entity>();

    private List<Entity> products2 = new ArrayList<Entity>();

    private List<Entity> products3 = new ArrayList<Entity>();

    private Entity product3 = new DefaultEntity("products", "product");

    private Entity product2 = new DefaultEntity("products", "product");

    private Entity product1 = new DefaultEntity("products", "product");

    private Entity technologyForQuantityPerTechnologyAlghorithm = new DefaultEntity("products", "technology");

    private Entity technologyForQuantityPerOutProductsAlghorithm = new DefaultEntity("products", "technology");

    private List<Entity> products4 = new ArrayList<Entity>();

    private Entity product4 = new DefaultEntity("products", "product");

    private List<Entity> products5 = new ArrayList<Entity>();

    private List<Entity> products6 = new ArrayList<Entity>();

    private List<Entity> products7 = new ArrayList<Entity>();

    private List<Entity> products8 = new ArrayList<Entity>();

    private List<Entity> products9 = new ArrayList<Entity>();

    private Entity product5 = new DefaultEntity("products", "product");

    private Entity product6 = new DefaultEntity("products", "product");

    @Before
    public void init() {
        reportDataService = new ReportDataService();
        EntityTree entityTreeSubTechnology = new EntityTree(dataDefinition, "technology", new Long(2));
        EntityList componentsList1 = new EntityList(dataDefinition, "operationProductInComponent", new Long(1));
        EntityList componentsList2 = new EntityList(dataDefinition, "operationProductInComponent", new Long(2));
        EntityList componentsList3 = new EntityList(dataDefinition, "operationProductInComponent", new Long(3));
        EntityList componentsOutList1 = new EntityList(dataDefinition, "operationProductOutComponent", new Long(11));
        EntityList componentsOutList2 = new EntityList(dataDefinition, "operationProductOutComponent", new Long(12));
        EntityList componentsOutList3 = new EntityList(dataDefinition, "operationProductOutComponent", new Long(13));
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
        Entity operationTechnology2 = new DefaultEntity("products", "technologyOperationComponent");
        operationTechnology2.setField("entityType", "technology");
        operationTechnology2.setField("parent", operationRoot);
        operationTechnology2.setId(new Long(5));
        operationTechnology2.setField("referenceTechnology", technology);

        Entity operationTechnology3 = new DefaultEntity("products", "technologyOperationComponent");
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
        Entity productInComponent1 = new DefaultEntity("products", "operationProductInComponent");
        productInComponent1.setField("product", product1);
        productInComponent1.setField("quantity", BigDecimal.ONE);
        product2.setField("typeOfMaterial", "01component");
        product2.setField("number", "2");
        product2.setId(new Long(2));
        Entity productInComponent2 = new DefaultEntity("products", "operationProductInComponent");
        productInComponent2.setField("product", product2);
        productInComponent2.setField("quantity", BigDecimal.ONE);
        product3.setField("typeOfMaterial", "02intermediate");
        product3.setField("number", "3");
        product3.setId(new Long(3));
        Entity productInComponent3 = new DefaultEntity("products", "operationProductInComponent");
        productInComponent3.setField("product", product3);
        productInComponent3.setField("quantity", BigDecimal.ONE);
        products1.add(productInComponent1);
        products2.add(productInComponent2);
        products2.add(productInComponent3);
        technologyForQuantityPerOutProductsAlghorithm.setField("operationComponents", entityTree);
        technologyForQuantityPerOutProductsAlghorithm.setField("componentQuantityAlgorithm", "01perProductOut");
        product4.setField("typeOfMaterial", "02intermediate");
        product4.setField("number", "4");
        product4.setId(new Long(4));
        Entity productInComponent4 = new DefaultEntity("products", "operationProductOutComponent");
        productInComponent4.setField("product", product4);
        productInComponent4.setField("quantity", BigDecimal.ONE);
        products4.add(productInComponent4);
        product5.setField("typeOfMaterial", "02intermediate");
        product5.setField("number", "5");
        product5.setId(new Long(5));
        Entity productInComponent5 = new DefaultEntity("products", "operationProductOutComponent");
        productInComponent5.setField("product", product5);
        productInComponent5.setField("quantity", BigDecimal.ONE);
        products5.add(productInComponent5);
        product6.setField("typeOfMaterial", "02intermediate");
        product6.setField("number", "6");
        product6.setId(new Long(6));
        Entity productInComponent6 = new DefaultEntity("products", "operationProductOutComponent");
        productInComponent6.setField("product", product6);
        productInComponent6.setField("quantity", BigDecimal.ONE);
        products6.add(productInComponent6);
        products7.add(productInComponent2);
        products8.add(productInComponent3);
        products9.add(productInComponent2);
        products9.add(productInComponent3);
        products9.add(productInComponent4);
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
        assertEquals(products.get(product1), new BigDecimal("5"));

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
        assertEquals(products.get(product2), new BigDecimal("5"));
        assertEquals(products.get(product3), new BigDecimal("5"));
        assertEquals(products.get(product5), new BigDecimal("5"));
        assertEquals(products.get(product6), new BigDecimal("5"));
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
        assertEquals(products.size(), 3);
        assertEquals(products.get(product2), new BigDecimal("10"));
        assertEquals(products.get(product3), new BigDecimal("5"));
        assertEquals(products.get(product5), new BigDecimal("5"));
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
        assertEquals(products.get(product2), new BigDecimal("12"));
        assertEquals(products.get(product3), new BigDecimal("7"));
        assertEquals(products.get(product5), new BigDecimal("5"));
        assertEquals(products.get(product6), new BigDecimal("5"));
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
        assertEquals(products.get(product2), BigDecimal.TEN);
        assertEquals(products.get(product3), BigDecimal.TEN);
        assertEquals(products.get(product4), BigDecimal.TEN);
        assertEquals(products.get(product5), BigDecimal.TEN);
        assertEquals(products.get(product6), BigDecimal.TEN);
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
        assertEquals(products.get(product2), new BigDecimal("5"));
        assertEquals(products.get(product3), new BigDecimal("5"));
        assertEquals(products.get(product4), new BigDecimal("5"));
        assertEquals(products.get(product5), new BigDecimal("5"));
        assertEquals(products.get(product6), new BigDecimal("5"));
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
}
