package com.qcadoo.mes.states.service.client;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.states.messages.MessagesHolder;
import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.states.messages.util.MessagesUtil;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.view.api.components.FormComponent;

public class StateChangeViewClientValidationUtilTest {

    private static final String TRANSLATION_KEY = "test.translation.key";

    private StateChangeViewClientValidationUtil validationUtil;

    @Mock
    private TranslationService translationService;

    @Mock
    private MessagesHolder messagesHolder;

    @Mock
    private FormComponent formComponent;

    @Mock
    private Entity entity;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        validationUtil = new StateChangeViewClientValidationUtil();

        ReflectionTestUtils.setField(validationUtil, "translationService", translationService);
    }

    @Test
    public final void shouldAddValidationErrorToEntityField() {
        // given
        final String existingFieldName = "existingField";

        FieldDefinition existingField = mockFieldDefinition(existingFieldName);
        DataDefinition dataDefinition = mockDataDefinition(Lists.newArrayList(existingField));
        given(entity.getDataDefinition()).willReturn(dataDefinition);
        given(formComponent.getEntity()).willReturn(entity);

        Entity message = mockValidationErrorMsg(existingFieldName, false, TRANSLATION_KEY);
        given(messagesHolder.getAllMessages()).willReturn(Lists.newArrayList(message));

        // when
        validationUtil.addValidationErrorMessages(formComponent, entity, messagesHolder);

        // then
        verify(entity).addError(existingField, TRANSLATION_KEY);
    }

    @Test
    public final void shouldAddValidationErrorToWholeEntityIfFieldDoesNotExist() {
        // given
        DataDefinition dataDefinition = mockDataDefinition(Lists.<FieldDefinition> newArrayList());
        given(entity.getDataDefinition()).willReturn(dataDefinition);
        given(formComponent.getEntity()).willReturn(entity);

        Entity message = mockValidationErrorMsg("notExistingField", false, TRANSLATION_KEY);
        given(messagesHolder.getAllMessages()).willReturn(Lists.newArrayList(message));

        // when
        validationUtil.addValidationErrorMessages(formComponent, entity, messagesHolder);

        // then
        verify(entity, Mockito.never()).addError(Mockito.any(FieldDefinition.class), Mockito.eq(TRANSLATION_KEY));
        verify(entity).addGlobalError(TRANSLATION_KEY, false);
    }

    @Test
    public final void shouldAddValidationErrorToWholeEntityIfFieldIsEmpty() {
        // given
        DataDefinition dataDefinition = mockDataDefinition(Lists.<FieldDefinition> newArrayList());
        given(entity.getDataDefinition()).willReturn(dataDefinition);
        given(formComponent.getEntity()).willReturn(entity);

        Entity message = mockValidationErrorMsg("", false, TRANSLATION_KEY);
        given(messagesHolder.getAllMessages()).willReturn(Lists.newArrayList(message));

        // when
        validationUtil.addValidationErrorMessages(formComponent, entity, messagesHolder);

        // then
        verify(entity, Mockito.never()).addError(Mockito.any(FieldDefinition.class), Mockito.eq(TRANSLATION_KEY));
        verify(entity).addGlobalError(TRANSLATION_KEY, false);
    }

    @Test
    public final void shouldAddValidationErrorToWholeEntityIfFieldIsNull() {
        // given
        DataDefinition dataDefinition = mockDataDefinition(Lists.<FieldDefinition> newArrayList());
        given(entity.getDataDefinition()).willReturn(dataDefinition);
        given(formComponent.getEntity()).willReturn(entity);

        Entity message = mockValidationErrorMsg(null, false, TRANSLATION_KEY);
        given(messagesHolder.getAllMessages()).willReturn(Lists.newArrayList(message));

        // when
        validationUtil.addValidationErrorMessages(formComponent, entity, messagesHolder);

        // then
        verify(entity, Mockito.never()).addError(Mockito.any(FieldDefinition.class), Mockito.eq(TRANSLATION_KEY));
        verify(entity).addGlobalError(TRANSLATION_KEY, false);
    }

    private DataDefinition mockDataDefinition(final Iterable<FieldDefinition> fieldDefinitions) {
        final DataDefinition dataDefinition = mock(DataDefinition.class);
        final Map<String, FieldDefinition> fields = Maps.newHashMap();
        for (final FieldDefinition fieldDefinition : fieldDefinitions) {
            fields.put(fieldDefinition.getName(), fieldDefinition);
        }

        given(dataDefinition.getFields()).willReturn(Collections.unmodifiableMap(fields));
        given(dataDefinition.getField(Mockito.anyString())).willAnswer(new Answer<FieldDefinition>() {

            @Override
            public FieldDefinition answer(final InvocationOnMock invocation) throws Throwable {
                return fields.get(invocation.getArguments()[0]);
            }
        });
        return dataDefinition;
    }

    private FieldDefinition mockFieldDefinition(final String name) {
        final FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getName()).willReturn(name);
        return fieldDefinition;
    }

    private Entity mockValidationErrorMsg(final String fieldName, final Boolean autoClose, final String translationKey,
            final String... args) {
        return mockMessage(StateMessageType.VALIDATION_ERROR, fieldName, autoClose, translationKey, args);
    }

    private Entity mockMessage(final StateMessageType type, final String fieldName, final Boolean autoClose,
            final String translationKey, final String... args) {
        final Entity message = mock(Entity.class);
        stubStringField(message, MessageFields.TYPE, type.getStringValue());
        stubStringField(message, MessageFields.CORRESPOND_FIELD_NAME, fieldName);
        stubStringField(message, MessageFields.TRANSLATION_KEY, translationKey);
        stubStringField(message, MessageFields.TRANSLATION_ARGS, MessagesUtil.joinArgs(args));
        stubBooleanField(message, MessageFields.AUTO_CLOSE, autoClose);
        return message;
    }

    private void stubStringField(final Entity entity, final String fieldName, final String fieldValue) {
        given(entity.getStringField(fieldName)).willReturn(fieldValue);
        given(entity.getField(fieldName)).willReturn(fieldValue);
    }

    private void stubBooleanField(final Entity entity, final String fieldName, final Boolean fieldValue) {
        given(entity.getField(fieldName)).willReturn(fieldValue);
        given(entity.getBooleanField(fieldName)).willReturn(fieldValue);
    }

}
