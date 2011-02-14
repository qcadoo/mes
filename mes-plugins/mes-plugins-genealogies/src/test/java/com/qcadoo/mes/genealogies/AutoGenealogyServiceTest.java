package com.qcadoo.mes.genealogies;

import static org.mockito.BDDMockito.given;
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

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        translationService = mock(TranslationService.class);
        autoGenealogyService = new AutoGenealogyService();
        setField(autoGenealogyService, "dataDefinitionService", dataDefinitionService);
        setField(autoGenealogyService, "translationService", translationService);
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
    public void shouldAutoCreateGenealogyWitAttributes() {
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

        given(translationService.translate("genealogies.message.autoGenealogy.genealogyExist", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.genealogyExist.pl");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state, times(2)).getFieldValue();
        verify(state).addMessage("genealogies.message.autoGenealogy.genealogyExist.pl test", MessageType.INFO);
    }
}
