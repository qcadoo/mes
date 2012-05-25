package com.qcadoo.mes.states;

import static com.qcadoo.mes.states.MessageArgsUtil.ARGS_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MessageArgsUtilTest {

    @Test
    public final void shouldReturnJoinedString() {
        // given
        final String[] splittedArgs = new String[] { "mes", "plugins", "states", "test" };

        // when
        final String joinedString = MessageArgsUtil.join(splittedArgs);

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
        final String[] splittedString = MessageArgsUtil.split(joinedArgs);

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
        final String[] splittedString = MessageArgsUtil.split(null);

        // then
        assertNotNull(splittedString);
        assertEquals(0, splittedString.length);
    }

    @Test
    public final void shouldReturnEmptyArrayIfGivenJoinedStringIsEmpty() {
        // when
        final String[] splittedString = MessageArgsUtil.split("");

        // then
        assertNotNull(splittedString);
        assertEquals(0, splittedString.length);
    }

    @Test
    public final void shouldReturnEmptyArrayIfGivenJoinedStringIsBlank() {
        // when
        final String[] splittedString = MessageArgsUtil.split("   ");

        // then
        assertNotNull(splittedString);
        assertEquals(0, splittedString.length);
    }

    @Test
    public final void shouldReturnNullIfGivenSplittedStringIsNull() {
        // when
        final String joinedString = MessageArgsUtil.join(null);

        // then
        assertNull(joinedString);
    }

    @Test
    public final void shouldReturnNullIfGivenSplittedStringIsEmpty() {
        // when
        final String joinedString = MessageArgsUtil.join(new String[] {});

        // then
        assertNull(joinedString);
    }

}
