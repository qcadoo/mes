package com.qcadoo.mes.technologies.tree.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentType;
import com.qcadoo.mes.technologies.tree.builder.api.InternalOperationProductComponent;
import com.qcadoo.mes.technologies.tree.builder.api.InternalTechnologyOperationComponent;
import com.qcadoo.mes.technologies.tree.builder.api.ItemWithQuantity;
import com.qcadoo.mes.technologies.tree.builder.api.OperationProductComponent;
import com.qcadoo.mes.technologies.tree.builder.api.OperationProductComponent.OperationCompType;
import com.qcadoo.mes.technologies.tree.builder.api.TechnologyOperationComponent;
import com.qcadoo.mes.technologies.tree.builder.api.TechnologyTreeAdapter;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;

public class TechnologyTreeBuildServiceImplTest {

    private TechnologyTreeBuildServiceImpl treeBuildService;

    @Mock
    private TechnologyTreeComponentsFactory componentsFactory;

    @Mock
    private Entity toc2op, toc2opic1prod, toc2opoc1prod;

    @Mock
    private Entity toc1op, toc1opic1prod, toc1opoc1prod;

    @Mock
    private NumberService numberService;

    @Captor
    private ArgumentCaptor<Collection<Entity>> toc1opicCaptor, toc1opocCaptor, toc2opicCaptor, toc2opocCaptor,
            toc1childrenCaptor, toc2childrenCaptor;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        treeBuildService = new TechnologyTreeBuildServiceImpl();
        given(componentsFactory.buildToc()).willAnswer(new Answer<InternalTechnologyOperationComponent>() {

            @Override
            public InternalTechnologyOperationComponent answer(final InvocationOnMock invocation) throws Throwable {
                return buildToc();
            }
        });
        given(componentsFactory.buildOpc(Mockito.any(OperationCompType.class))).willAnswer(
                new Answer<InternalOperationProductComponent>() {

                    @Override
                    public InternalOperationProductComponent answer(final InvocationOnMock invocation) throws Throwable {
                        return buildOpc((OperationCompType) invocation.getArguments()[0]);
                    }
                });

        ReflectionTestUtils.setField(treeBuildService, "componentsFactory", componentsFactory);
        ReflectionTestUtils.setField(treeBuildService, "numberService", numberService);
    }

    private InternalOperationProductComponent buildOpc(final OperationCompType type) {
        DataDefinition dataDef = mock(DataDefinition.class);
        given(dataDef.getPluginIdentifier()).willReturn(TechnologiesConstants.PLUGIN_IDENTIFIER);
        given(dataDef.getName()).willReturn(type.getModelName());
        Entity wrappedEntity = mock(Entity.class);
        given(dataDef.create()).willReturn(wrappedEntity);
        given(wrappedEntity.copy()).willReturn(wrappedEntity);
        return new OperationProductComponentImpl(type, dataDef);
    }

    private InternalTechnologyOperationComponent buildToc() {
        DataDefinition dataDef = mock(DataDefinition.class);
        given(dataDef.getPluginIdentifier()).willReturn(TechnologiesConstants.PLUGIN_IDENTIFIER);
        given(dataDef.getName()).willReturn(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        Entity wrappedEntity = mock(Entity.class);
        given(dataDef.create()).willReturn(wrappedEntity);
        given(wrappedEntity.copy()).willReturn(wrappedEntity);
        return new TechnologyOperationCompImpl(dataDef);
    }

    private class TestTreeAdapter implements TechnologyTreeAdapter<TocHolder, ProductHolder> {

        @Override
        public void setOpCompCustomFields(final TechnologyOperationComponent toc, final TocHolder from) {
        }

        @Override
        public void setOpProductCompCustomFields(final OperationProductComponent opc, final ProductHolder from) {
        }

        @Override
        public Collection<ItemWithQuantity<ProductHolder>> extractInputProducts(final TocHolder from) {
            return from.getInputs();
        }

        @Override
        public Collection<ItemWithQuantity<ProductHolder>> extractOutputProducts(final TocHolder from) {
            return from.getOutputs();
        }

        @Override
        public Iterable<TocHolder> extractSubOperations(final TocHolder from) {
            return from.getSubOps();
        }

        @Override
        public Entity buildProductEntity(final ProductHolder from) {
            return from.getProduct();
        }

        @Override
        public Entity buildOperationEntity(final TocHolder from) {
            return from.getOperation();
        }

    }

    private interface TocHolder {

        Collection<ItemWithQuantity<ProductHolder>> getInputs();

        Collection<ItemWithQuantity<ProductHolder>> getOutputs();

        Entity getOperation();

        Iterable<TocHolder> getSubOps();
    }

    private interface ProductHolder {

        Entity getProduct();
    }

    private TocHolder mockCustomTreeRepresentation() {
        DataDefinition toc2opDataDef = mockOperationDataDef();
        given(toc2op.getDataDefinition()).willReturn(toc2opDataDef);

        DataDefinition toc1opDataDef = mockOperationDataDef();
        given(toc1op.getDataDefinition()).willReturn(toc1opDataDef);

        ItemWithQuantity<ProductHolder> toc2input1 = mockProduct(toc2opic1prod, BigDecimal.ONE);
        Collection<ItemWithQuantity<ProductHolder>> toc2inputs = Lists.newArrayList();
        toc2inputs.add(toc2input1);

        ItemWithQuantity<ProductHolder> toc2output1 = mockProduct(toc2opoc1prod, BigDecimal.ONE);
        Collection<ItemWithQuantity<ProductHolder>> toc2outputs = Lists.newArrayList();
        toc2outputs.add(toc2output1);

        TocHolder toc2 = mockTocNode(toc2op, toc2inputs, toc2outputs, Collections.<TocHolder> emptyList());

        ItemWithQuantity<ProductHolder> toc1input1 = mockProduct(toc1opic1prod, BigDecimal.ONE);
        Collection<ItemWithQuantity<ProductHolder>> toc1inputs = Lists.newArrayList();
        toc1inputs.add(toc1input1);

        ItemWithQuantity<ProductHolder> toc1output1 = mockProduct(toc1opoc1prod, BigDecimal.ONE);
        Collection<ItemWithQuantity<ProductHolder>> toc1outputs = Lists.newArrayList();
        toc1outputs.add(toc1output1);

        return mockTocNode(toc1op, toc1inputs, toc1outputs, Lists.newArrayList(toc2));
    }

    private ItemWithQuantity<ProductHolder> mockProduct(final Entity productEntity, final BigDecimal quantity) {
        ProductHolder product = mock(ProductHolder.class);
        given(product.getProduct()).willReturn(productEntity);
        given(numberService.setScale(quantity)).willReturn(quantity);
        DataDefinition dataDef = mockProductDataDef();
        given(productEntity.getDataDefinition()).willReturn(dataDef);
        return new ItemWithQuantity<ProductHolder>(product, quantity);
    }

    private DataDefinition mockOperationDataDef() {
        return mockDataDefinition(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION);
    }

    private DataDefinition mockProductDataDef() {
        return mockDataDefinition(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    private DataDefinition mockDataDefinition(final String pluginId, final String modelName) {
        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.getPluginIdentifier()).willReturn(pluginId);
        given(dataDefinition.getName()).willReturn(modelName);
        return dataDefinition;
    }

    private TocHolder mockTocNode(final Entity operation, final Collection<ItemWithQuantity<ProductHolder>> inputs,
            final Collection<ItemWithQuantity<ProductHolder>> outputs, final Iterable<TocHolder> subOps) {
        TocHolder tocNode = mock(TocHolder.class);
        given(tocNode.getOperation()).willReturn(operation);
        given(tocNode.getInputs()).willReturn(inputs);
        given(tocNode.getOutputs()).willReturn(outputs);
        given(tocNode.getSubOps()).willReturn(subOps);
        return tocNode;
    }

    @Test
    public final void shouldBuildTree() {
        // given
        TocHolder customTreeRoot = mockCustomTreeRepresentation();

        // when
        EntityTree tree = treeBuildService.build(customTreeRoot, new TestTreeAdapter());

        // then
        EntityTreeNode root = tree.getRoot();
        assertNotNull(root);
        Entity toc1 = extractEntity(root);
        verify(toc1).setField(TechnologyOperationComponentFields.ENTITY_TYPE,
                TechnologyOperationComponentType.OPERATION.getStringValue());
        verify(toc1).setField(TechnologyOperationComponentFields.OPERATION, toc1op);

        verify(toc1).setField(Mockito.eq(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS),
                toc1opicCaptor.capture());
        Collection<Entity> toc1opics = toc1opicCaptor.getValue();
        assertEquals(1, toc1opics.size());
        Entity toc1opic = toc1opics.iterator().next();
        verify(toc1opic).setField(OperationProductInComponentFields.QUANTITY, BigDecimal.ONE);
        verify(toc1opic).setField(OperationProductInComponentFields.PRODUCT, toc1opic1prod);

        verify(toc1).setField(Mockito.eq(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS),
                toc1opocCaptor.capture());
        Collection<Entity> toc1opocs = toc1opocCaptor.getValue();
        assertEquals(1, toc1opocs.size());
        Entity toc1opoc = toc1opocs.iterator().next();
        verify(toc1opoc).setField(OperationProductInComponentFields.QUANTITY, BigDecimal.ONE);
        verify(toc1opoc).setField(OperationProductInComponentFields.PRODUCT, toc1opoc1prod);

        verify(toc1).setField(Mockito.eq(TechnologyOperationComponentFields.CHILDREN), toc1childrenCaptor.capture());
        Collection<Entity> toc1children = toc1childrenCaptor.getValue();
        assertEquals(1, toc1children.size());

        Entity toc2 = toc1children.iterator().next();

        verify(toc2).setField(TechnologyOperationComponentFields.ENTITY_TYPE,
                TechnologyOperationComponentType.OPERATION.getStringValue());
        verify(toc2).setField(TechnologyOperationComponentFields.OPERATION, toc2op);

        verify(toc2).setField(Mockito.eq(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS),
                toc2opicCaptor.capture());
        Collection<Entity> toc2opics = toc2opicCaptor.getValue();
        assertEquals(1, toc2opics.size());
        Entity toc2opic = toc2opics.iterator().next();
        verify(toc2opic).setField(OperationProductInComponentFields.QUANTITY, BigDecimal.ONE);
        verify(toc2opic).setField(OperationProductInComponentFields.PRODUCT, toc2opic1prod);

        verify(toc2).setField(Mockito.eq(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS),
                toc2opocCaptor.capture());
        Collection<Entity> toc2opocs = toc2opocCaptor.getValue();
        assertEquals(1, toc2opocs.size());
        Entity toc2opoc = toc2opocs.iterator().next();
        verify(toc2opoc).setField(OperationProductInComponentFields.QUANTITY, BigDecimal.ONE);
        verify(toc2opoc).setField(OperationProductInComponentFields.PRODUCT, toc2opoc1prod);

        verify(toc2).setField(Mockito.eq(TechnologyOperationComponentFields.CHILDREN), toc2childrenCaptor.capture());
        Collection<Entity> toc2children = toc2childrenCaptor.getValue();
        assertTrue(toc2children.isEmpty());
    }

    private Entity extractEntity(final Entity wrapper) {
        return (Entity) ReflectionTestUtils.getField(wrapper, "entity");
    }

}
