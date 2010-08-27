package com.qcadoo.mes.plugins.products.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ValidationResultFactoryInvalidResultTest {

    @Test
    public void shouldReturnValidResultWhenInvalid() {
        // given
        String globalMsg = "global";
        Map<String, String> fieldMessages = new HashMap<String, String>();
        fieldMessages.put("testKey", "testValue");

        // when
        ValidationResult result = ValidationResultFactory.createInvalidResult(globalMsg, fieldMessages);

        // then
        assertEquals(false, result.isValid());
        assertNull(result.getValidEntity());
        assertEquals(globalMsg, result.getGlobalMessage());
        assertEquals(fieldMessages, result.getFieldMessages());
        assertEquals(fieldMessages.size(), result.getFieldMessages().size());
        assertEquals(fieldMessages.get("testKey"), result.getFieldMessages().get("testKey"));
    }
}
