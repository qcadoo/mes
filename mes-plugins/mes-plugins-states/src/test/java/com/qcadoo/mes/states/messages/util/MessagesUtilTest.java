package com.qcadoo.mes.states.messages.util;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.ARGS_SEPARATOR;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.joinArgs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class MessagesUtilTest {

    @Test
    public final void shouldReturnJoinedString() {
        // given
        final String[] splittedArgs = new String[] { "mes", "plugins", "states", "test" };

        // when
        final String joinedString = MessagesUtil.joinArgs(splittedArgs);

        // then
        final String expectedString = splittedArgs[0] + ARGS_SEPARATOR + splittedArgs[1] + ARGS_SEPARATOR + splittedArgs[2]
                + ARGS_SEPARATOR + splittedArgs[3];
        assertEquals(expectedString, joinedString);

    }

    @Test
    public final void shouldReturnSplittedString() {
        // given
        final String arg1 = "mes";
        final String arg2 = "plugins";
        final String arg3 = "states";
        final String arg4 = "test";
        final String joinedArgs = arg1 + ARGS_SEPARATOR + arg2 + ARGS_SEPARATOR + arg3 + ARGS_SEPARATOR + arg4;

        // when
        final String[] splittedString = MessagesUtil.splitArgs(joinedArgs);

        // then
        assertEquals(4, splittedString.length);
        assertEquals(arg1, splittedString[0]);
        assertEquals(arg2, splittedString[1]);
        assertEquals(arg3, splittedString[2]);
        assertEquals(arg4, splittedString[3]);
    }

    @Test
    public final void shouldReturnEmptyArrayIfGivenJoinedStringIsNull() {
        // when
        final String[] splittedString = MessagesUtil.splitArgs(null);

        // then
        assertNotNull(splittedString);
        assertEquals(0, splittedString.length);
    }

    @Test
    public final void shouldReturnEmptyArrayIfGivenJoinedStringIsEmpty() {
        // when
        final String[] splittedString = MessagesUtil.splitArgs("");

        // then
        assertNotNull(splittedString);
        assertEquals(0, splittedString.length);
    }

    @Test
    public final void shouldReturnEmptyArrayIfGivenJoinedStringIsBlank() {
        // when
        final String[] splittedString = MessagesUtil.splitArgs("   ");

        // then
        assertNotNull(splittedString);
        assertEquals(0, splittedString.length);
    }

    @Test
    public final void shouldReturnNullIfGivenSplittedStringIsNull() {
        // when
        final String joinedString = MessagesUtil.joinArgs(null);

        // then
        assertNull(joinedString);
    }

    @Test
    public final void shouldReturnNullIfGivenSplittedStringIsEmpty() {
        // when
        final String joinedString = MessagesUtil.joinArgs(new String[] {});

        // then
        assertNull(joinedString);
    }

    @Test
    public final void shouldHasFailureMessagesReturnTrue() {
        // given
        List<Entity> messages = Lists.newArrayList();
        messages.add(mockMessage(MessageType.FAILURE, "test"));
        EntityList messagesEntityList = mockEntityList(messages);

        // when
        boolean result = MessagesUtil.hasFailureMessages(messagesEntityList);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldHasFailureMessagesReturnFalse() {
        // given
        List<Entity> messages = Lists.newArrayList();
        messages.add(mockMessage(MessageType.SUCCESS, "test"));
        EntityList messagesEntityList = mockEntityList(messages);

        // when
        boolean result = MessagesUtil.hasFailureMessages(messagesEntityList);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldHasFailureMessagesReturnFalseForEmptyMessages() {
        // given
        List<Entity> messages = Lists.newArrayList();
        EntityList messagesEntityList = mockEntityList(messages);

        // when
        boolean result = MessagesUtil.hasFailureMessages(messagesEntityList);

        // then
        assertFalse(result);
    }

    private EntityList mockEntityList(final List<Entity> entities) {
        EntityList entityList = mock(EntityList.class);
        given(entityList.iterator()).willReturn(entities.iterator());
        given(entityList.isEmpty()).willReturn(entities.isEmpty());
        return entityList;
    }

    private Entity mockMessage(final MessageType type, final String translationKey, final String... translationArgs) {
        Entity message = mock(Entity.class);
        mockEntityField(message, MessageFields.TYPE, type);
        mockEntityField(message, MessageFields.TRANSLATION_KEY, translationKey);
        mockEntityField(message, MessageFields.TRANSLATION_ARGS, joinArgs(translationArgs));
        return message;
    }

    private void mockEntityField(final Entity entity, final String fieldName, final Object fieldValue) {
        given(entity.getField(fieldName)).willReturn(fieldValue);
        given(entity.getStringField(fieldName)).willReturn(fieldValue == null ? null : fieldValue.toString());
    }

}
