/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.states;

import static com.qcadoo.mes.states.constants.StateChangeStatus.FAILURE;
import static org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;

public class StateChangeContextTest extends StateChangeTest {

    @Mock
    private MessageService messageService;

    @Mock
    private Entity savedOwner;

    private StateChangeEntityDescriber describer;

    private StateChangeContext stateChangeContext;

    private static final String FIELD_1_NAME = "field_1";

    private static final String FIELD_1_MESSAGE_1 = "field_1_message_1";

    private static final String GLOBAL_MESSAGE_1 = "global_message_1";

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        describer = new MockStateChangeDescriber(stateChangeDD);
        stubStateChangeEntity(describer);
        stubOwner();
        stubBelongsToField(stateChangeEntity, describer.getOwnerFieldName(), owner);
        stateChangeContext = new StateChangeContextImpl(stateChangeEntity, describer, messageService);
    }

    @Test
    public final void shouldSave() {
        // when
        stateChangeContext.save();

        // then
        verify(stateChangeDD, atLeastOnce()).save(stateChangeEntity);
        verify(messageService, never()).addValidationError(Mockito.eq(stateChangeContext), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(String[].class));
        verify(messageService, never()).addValidationError(Mockito.eq(stateChangeContext), Mockito.eq((String) null),
                Mockito.anyString(), Mockito.any(String[].class));
        verify(stateChangeEntity, never()).setField(describer.getStatusFieldName(), FAILURE.getStringValue());
    }

    @Test
    public final void shouldThrowExceptionIfConstructorGetInvalidStateChangeEntity() {
        // given
        given(stateChangeEntity.isValid()).willReturn(false);

        // when
        try {
            new StateChangeContextImpl(stateChangeEntity, describer, messageService);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public final void shouldMarkExistingDatabaseEntityAsInvalidIfConstructorGetInvalidStateChangeEntity() {
        // given
        final Entity existingStateChangeEntity = mock(Entity.class);
        given(stateChangeEntity.isValid()).willReturn(false);
        given(stateChangeEntity.getId()).willReturn(1L);
        given(stateChangeDD.get(Mockito.any(Long.class))).willReturn(existingStateChangeEntity);
        given(existingStateChangeEntity.isValid()).willReturn(true);

        final Map<String, ErrorMessage> fieldErrorsMap = Maps.newHashMap();
        final ErrorMessage fieldErrorMessage = buildErrorMessage(FIELD_1_MESSAGE_1);
        fieldErrorsMap.put(FIELD_1_NAME, fieldErrorMessage);
        given(stateChangeEntity.getErrors()).willReturn(fieldErrorsMap);

        final List<ErrorMessage> globalErrors = Lists.newArrayList();
        final ErrorMessage globalErrorMessage = buildErrorMessage(GLOBAL_MESSAGE_1);
        globalErrors.add(globalErrorMessage);
        given(stateChangeEntity.getGlobalErrors()).willReturn(globalErrors);

        // when
        new StateChangeContextImpl(stateChangeEntity, describer, messageService);

        // then
        verify(messageService).addValidationError(Mockito.any(StateChangeContext.class), Mockito.eq(FIELD_1_NAME),
                Mockito.eq(FIELD_1_MESSAGE_1));
        verify(messageService).addValidationError(Mockito.any(StateChangeContext.class), Mockito.eq((String) null),
                Mockito.eq(GLOBAL_MESSAGE_1));
        verify(existingStateChangeEntity).setField(describer.getStatusFieldName(), FAILURE.getStringValue());
    }

    @Test
    public final void shouldMarkEntityAsInvalidAndSetStateToFailureIfStateChangeEntityIsInvalidAfterSave() {
        // given
        final Entity savedStateChangeEntity = mock(Entity.class);
        given(stateChangeEntity.isValid()).willReturn(true);
        given(savedStateChangeEntity.isValid()).willReturn(true, false);
        given(stateChangeDD.save(stateChangeEntity)).willReturn(savedStateChangeEntity);

        final Map<String, ErrorMessage> fieldErrorsMap = Maps.newHashMap();
        final ErrorMessage fieldErrorMessage = buildErrorMessage(FIELD_1_MESSAGE_1);
        fieldErrorsMap.put(FIELD_1_NAME, fieldErrorMessage);
        given(savedStateChangeEntity.getErrors()).willReturn(fieldErrorsMap);

        final List<ErrorMessage> globalErrors = Lists.newArrayList();
        final ErrorMessage globalErrorMessage = buildErrorMessage(GLOBAL_MESSAGE_1);
        globalErrors.add(globalErrorMessage);
        given(savedStateChangeEntity.getGlobalErrors()).willReturn(globalErrors);

        // when
        stateChangeContext.save();

        // then
        verify(messageService).addValidationError(stateChangeContext, FIELD_1_NAME, FIELD_1_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(messageService).addValidationError(stateChangeContext, null, GLOBAL_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(stateChangeEntity).setField(describer.getStatusFieldName(), FAILURE.getStringValue());
    }

    @Test
    public final void shouldMarkEntityAsFailureAndRethrowExceptionIfOwnerValidatorThrowsException() {
        // given
        given(ownerDD.save(owner)).willThrow(new RuntimeException());

        try {
            // when
            stateChangeContext.setOwner(owner);
        } catch (StateChangeException e) {
            // then
            verify(stateChangeEntity).setField(describer.getStatusFieldName(), FAILURE.getStringValue());
        }
    }

    @Test
    public final void shouldSaveAndSetOwnerEntity() {
        // given
        given(owner.isValid()).willReturn(true);
        given(savedOwner.isValid()).willReturn(true);
        given(ownerDD.save(owner)).willReturn(savedOwner);

        // when
        stateChangeContext.setOwner(owner);

        // then
        verify(ownerDD).save(owner);
        verify(stateChangeEntity, never()).setField(describer.getOwnerFieldName(), owner);
        verify(stateChangeEntity).setField(describer.getOwnerFieldName(), savedOwner);
        verify(messageService, never()).addValidationError(Mockito.eq(stateChangeContext), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(String[].class));
        verify(messageService, never()).addValidationError(Mockito.eq(stateChangeContext), Mockito.eq((String) null),
                Mockito.anyString(), Mockito.any(String[].class));
        verify(stateChangeEntity, never()).setField(describer.getStatusFieldName(), StateChangeStatus.FAILURE.getStringValue());
    }

    @Test
    public final void shouldNotSaveAndSetAlreadyInvalidOwnerEntity() {
        // given
        given(owner.isValid()).willReturn(false);
        given(ownerDD.save(owner)).willReturn(owner);

        final Map<String, ErrorMessage> fieldErrorsMap = Maps.newHashMap();
        final ErrorMessage fieldErrorMessage = buildErrorMessage(FIELD_1_MESSAGE_1);
        fieldErrorsMap.put(FIELD_1_NAME, fieldErrorMessage);
        given(owner.getErrors()).willReturn(fieldErrorsMap);

        final List<ErrorMessage> globalErrors = Lists.newArrayList();
        final ErrorMessage globalErrorMessage = buildErrorMessage(GLOBAL_MESSAGE_1);
        globalErrors.add(globalErrorMessage);
        given(owner.getGlobalErrors()).willReturn(globalErrors);

        // when
        stateChangeContext.setOwner(owner);

        // then
        verify(ownerDD, never()).save(owner);
        verify(stateChangeEntity, never()).setField(describer.getOwnerFieldName(), owner);
        verify(stateChangeEntity, never()).setField(describer.getOwnerFieldName(), savedOwner);
        verify(messageService).addValidationError(stateChangeContext, FIELD_1_NAME, FIELD_1_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(messageService).addValidationError(stateChangeContext, null, GLOBAL_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(stateChangeEntity).setField(describer.getStatusFieldName(), StateChangeStatus.FAILURE.getStringValue());
    }

    @Test
    public final void shouldAddMessagesOnlyOnce() {
        // given
        given(owner.isValid()).willReturn(false);
        given(ownerDD.save(owner)).willReturn(owner);

        final Map<String, ErrorMessage> fieldErrorsMap = Maps.newHashMap();
        final ErrorMessage fieldErrorMessage = buildErrorMessage(FIELD_1_MESSAGE_1);
        fieldErrorsMap.put(FIELD_1_NAME, fieldErrorMessage);
        given(owner.getErrors()).willReturn(fieldErrorsMap);

        final List<ErrorMessage> globalErrors = Lists.newArrayList();
        final ErrorMessage globalErrorMessage = buildErrorMessage(GLOBAL_MESSAGE_1);
        globalErrors.add(globalErrorMessage);
        given(owner.getGlobalErrors()).willReturn(globalErrors);

        // when
        stateChangeContext.setOwner(owner);
        stateChangeContext.setOwner(owner);

        // then
        verify(ownerDD, never()).save(owner);
        verify(stateChangeEntity, never()).setField(describer.getOwnerFieldName(), owner);
        verify(stateChangeEntity, never()).setField(describer.getOwnerFieldName(), savedOwner);
        verify(messageService).addValidationError(stateChangeContext, FIELD_1_NAME, FIELD_1_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(messageService).addValidationError(stateChangeContext, null, GLOBAL_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(stateChangeEntity).setField(describer.getStatusFieldName(), StateChangeStatus.FAILURE.getStringValue());
    }

    @Test
    public final void shouldSaveButNotSetJustInvalidateOwnerEntity() {
        // given
        given(owner.isValid()).willReturn(true);
        given(ownerDD.save(owner)).willReturn(savedOwner);
        given(savedOwner.isValid()).willReturn(false);

        final Map<String, ErrorMessage> fieldErrorsMap = Maps.newHashMap();
        final ErrorMessage fieldErrorMessage = buildErrorMessage(FIELD_1_MESSAGE_1);
        fieldErrorsMap.put(FIELD_1_NAME, fieldErrorMessage);
        given(savedOwner.getErrors()).willReturn(fieldErrorsMap);

        final List<ErrorMessage> globalErrors = Lists.newArrayList();
        final ErrorMessage globalErrorMessage = buildErrorMessage(GLOBAL_MESSAGE_1);
        globalErrors.add(globalErrorMessage);
        given(savedOwner.getGlobalErrors()).willReturn(globalErrors);

        // when
        stateChangeContext.setOwner(owner);

        // then
        verify(ownerDD).save(owner);
        verify(stateChangeEntity, never()).setField(describer.getOwnerFieldName(), owner);
        verify(stateChangeEntity, never()).setField(describer.getOwnerFieldName(), savedOwner);
        verify(messageService).addValidationError(stateChangeContext, FIELD_1_NAME, FIELD_1_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(messageService).addValidationError(stateChangeContext, null, GLOBAL_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(stateChangeEntity).setField(describer.getStatusFieldName(), StateChangeStatus.FAILURE.getStringValue());
    }

    @Test
    public final void shouldCopyValidationErrorMessagesFromEntity() {
        // given
        given(owner.isValid()).willReturn(false);

        final Map<String, ErrorMessage> fieldErrorsMap = Maps.newHashMap();
        final ErrorMessage fieldErrorMessage = buildErrorMessage(FIELD_1_MESSAGE_1);
        fieldErrorsMap.put(FIELD_1_NAME, fieldErrorMessage);
        given(owner.getErrors()).willReturn(fieldErrorsMap);

        final List<ErrorMessage> globalErrors = Lists.newArrayList();
        final ErrorMessage globalErrorMessage = buildErrorMessage(GLOBAL_MESSAGE_1);
        globalErrors.add(globalErrorMessage);
        given(owner.getGlobalErrors()).willReturn(globalErrors);

        // when
        stateChangeContext.setOwner(owner);

        // then
        verify(messageService).addValidationError(stateChangeContext, FIELD_1_NAME, FIELD_1_MESSAGE_1, EMPTY_STRING_ARRAY);
        verify(messageService).addValidationError(stateChangeContext, null, GLOBAL_MESSAGE_1, EMPTY_STRING_ARRAY);
    }

    private ErrorMessage buildErrorMessage(final String message) {
        return new ErrorMessage(message, EMPTY_STRING_ARRAY);
    }
}
