package com.qcadoo.mes.genealogies;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.EntityList;
import com.qcadoo.mes.internal.EntityTree;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.Restriction;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FormComponentState.class })
public class AutoGenealogyServiceTest {

    private AutoGenealogyService autoGenealogyService;

    private DataDefinitionService dataDefinitionService;

    private TranslationService translationService;

    private GenealogyService genealogyService;

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        translationService = mock(TranslationService.class);
        genealogyService = new GenealogyService();
        autoGenealogyService = new AutoGenealogyService();
        setField(autoGenealogyService, "dataDefinitionService", dataDefinitionService);
        setField(autoGenealogyService, "translationService", translationService);
        setField(autoGenealogyService, "genealogyService", genealogyService);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfNoRowIsSelected() throws Exception {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(null);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(translationService.translate("core.grid.noRowSelectedError", Locale.ENGLISH)).willReturn(
                "core.grid.noRowSelectedError.pl");

        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state).addMessage("core.grid.noRowSelectedError.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfFormHasNoIdentifier() throws Exception {
        // given
        FormComponentState state = mock(FormComponentState.class);
        given(state.getFieldValue()).willReturn(null);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(translationService.translate("core.form.entityWithoutIdentifier", Locale.ENGLISH)).willReturn(
                "core.form.entityWithoutIdentifier.pl");

        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state).addMessage("core.form.entityWithoutIdentifier.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfOrderIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        given(dataDefinitionService.get("products", "order").get(13L)).willReturn(null);

        given(translationService.translate("core.message.entityNotFound", Locale.ENGLISH)).willReturn(
                "core.message.entityNotFound.pl");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state, times(2)).getFieldValue();
        verify(state).addMessage("core.message.entityNotFound.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfProductIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(null);

        given(dataDefinitionService.get("products", "order").get(13L)).willReturn(order);

        given(translationService.translate("genealogies.message.autoGenealogy.failure.product", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.failure.product.pl");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state, times(2)).getFieldValue();
        verify(state).addMessage("genealogies.message.autoGenealogy.failure.product.pl", MessageType.INFO);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfTechnologyIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);
        Entity product = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(product);
        given(order.getBelongsToField("technology")).willReturn(null);

        given(dataDefinitionService.get("products", "order").get(13L)).willReturn(order);

        given(translationService.translate("genealogies.message.autoGenealogy.failure.product", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.failure.product.pl");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state, times(2)).getFieldValue();
        verify(state).addMessage("genealogies.message.autoGenealogy.failure.product.pl", MessageType.INFO);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfMainBatchIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(product);
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(product.getField("number")).willReturn("test");
        given(product.getField("name")).willReturn("test");

        given(dataDefinitionService.get("products", "order").get(13L)).willReturn(order);

        given(translationService.translate("genealogies.message.autoGenealogy.missingMainBatch", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.missingMainBatch.pl");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state, times(2)).getFieldValue();
        verify(state).addMessage("genealogies.message.autoGenealogy.missingMainBatch.pltest-test", MessageType.INFO, false);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfExistingGenealogyWithBatch() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(product);
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(product.getField("batch")).willReturn("test");

        given(dataDefinitionService.get("products", "order").get(13L)).willReturn(order);

        List<Entity> list = new ArrayList<Entity>();
        list.add(mock(Entity.class));
        given(
                dataDefinitionService.get("genealogies", "genealogy").find().restrictedWith(Restrictions.eq("batch", "test"))
                        .restrictedWith(Restrictions.eq("order.id", order.getId())).withMaxResults(1).list().getEntities())
                .willReturn(list);

        given(translationService.translate("genealogies.message.autoGenealogy.genealogyExist", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.genealogyExist.pl");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state, times(2)).getFieldValue();
        verify(state).addMessage("genealogies.message.autoGenealogy.genealogyExist.pl test", MessageType.INFO);
    }

    @Test
    public void shouldAutoCreateGenealogyWithoutAttributes() {
        // given
        /*
         * ComponentState state = mock(ComponentState.class); given(state.getFieldValue()).willReturn(13L);
         * given(state.getLocale()).willReturn(Locale.ENGLISH); ViewDefinitionState viewDefinitionState =
         * mock(ViewDefinitionState.class); Entity order = mock(Entity.class); Entity product = mock(Entity.class); Entity
         * technology = mock(Entity.class); given(order.getBelongsToField("product")).willReturn(product);
         * given(order.getBelongsToField("technology")).willReturn(technology);
         * given(product.getField("batch")).willReturn("test"); given(dataDefinitionService.get("products",
         * "order").get(13L)).willReturn(order); // List<Entity> list = new ArrayList<Entity>(); //
         * given(dataDefinitionService.get("genealogies", "currentAttribute").find().withMaxResults(1).list().getEntities()) //
         * .willReturn(list); given(technology.getField("shiftFeatureRequired")).willReturn(false);
         * given(technology.getField("postFeatureRequired")).willReturn(false);
         * given(technology.getField("otherFeatureRequired")).willReturn(false); EntityTree operationProductInComponents =
         * prepareOperationProductInComponents();
         * given(technology.getTreeField("operationComponents")).willReturn(operationProductInComponents);
         * given(translationService.translate("genealogies.message.autoGenealogy.genealogyExist", Locale.ENGLISH)).willReturn(
         * "genealogies.message.autoGenealogy.genealogyExist.pl"); // when
         * autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" }); // then verify(state,
         * times(2)).getFieldValue(); verify(state).addMessage("genealogies.message.autoGenealogy.genealogyExist.pl test",
         * MessageType.INFO);
         */
    }

    @SuppressWarnings("unchecked")
    private EntityTree prepareOperationProductInComponents() {
        List<Entity> entities = new ArrayList<Entity>();
        List<Entity> subEntities = new ArrayList<Entity>();
        List<Entity> productsEntities1 = new ArrayList<Entity>();
        List<Entity> productsEntities3 = new ArrayList<Entity>();

        productsEntities1.add(craeteOperationProductInComponent(101L, true));
        productsEntities3.add(craeteOperationProductInComponent(103L, true));
        productsEntities3.add(craeteOperationProductInComponent(104L, false));

        DataDefinition treeDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(treeDataDefinition.find().restrictedWith(any(Restriction.class)).orderAscBy(eq("priority")).list().getEntities())
                .willReturn(entities, subEntities);

        DataDefinition listDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(listDataDefinition.find().restrictedWith(any(Restriction.class)).list().getEntities()).willReturn(
                productsEntities1, productsEntities3);

        EntityTree subOperationComponents = new EntityTree(treeDataDefinition, "joinField", 13L);

        EntityList operationProductInComponents1 = new EntityList(listDataDefinition, "joinField", 1L);
        EntityList operationProductInComponents3 = new EntityList(listDataDefinition, "joinField", 3L);

        Entity operationComponent1 = mock(Entity.class);
        given(operationComponent1.getId()).willReturn(1L);
        given(operationComponent1.getField("entityType")).willReturn("operation");
        given(operationComponent1.getHasManyField("operationProductInComponents")).willReturn(operationProductInComponents1);
        given(operationComponent1.getBelongsToField("parent")).willReturn(null);

        Entity referenceTechnology = mock(Entity.class);
        given(referenceTechnology.getTreeField("operationComponents")).willReturn(subOperationComponents);

        Entity operationComponent2 = mock(Entity.class);
        given(operationComponent2.getId()).willReturn(2L);
        given(operationComponent2.getField("entityType")).willReturn("referenceTechnology");
        given(operationComponent2.getBelongsToField("referenceTechnology")).willReturn(referenceTechnology);
        given(operationComponent2.getBelongsToField("parent")).willReturn(operationComponent1);

        Entity operationComponent3 = mock(Entity.class);
        given(operationComponent3.getId()).willReturn(3L);
        given(operationComponent3.getField("entityType")).willReturn("operation");
        given(operationComponent3.getHasManyField("operationProductInComponents")).willReturn(operationProductInComponents3);
        given(operationComponent3.getBelongsToField("parent")).willReturn(null);

        entities.add(operationComponent1);
        entities.add(operationComponent2);
        subEntities.add(operationComponent3);

        EntityTree operationComponents = new EntityTree(treeDataDefinition, "joinField", 13L);
        return operationComponents;
    }

    private Entity craeteOperationProductInComponent(final Long id, final boolean batchRequired) {
        Entity operationProductInComponent = new DefaultEntity("genealogies", "genealogyProductInComponent", id);
        operationProductInComponent.setField("batchRequired", batchRequired);
        return operationProductInComponent;
    }
}
