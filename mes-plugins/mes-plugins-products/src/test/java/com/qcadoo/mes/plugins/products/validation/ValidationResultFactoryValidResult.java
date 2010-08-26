package com.qcadoo.mes.plugins.products.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.qcadoo.mes.core.data.beans.Entity;

public class ValidationResultFactoryValidResult {

    @Test
    public void shouldReturnValidResultWhenValid() {
        // given
        Entity entity = new Entity();

        // when
        ValidationResult result = ValidationResultFactory.getInstance().createValidResult(entity);

        // then
        assertEquals(true, result.isValid());
        assertEquals(entity, result.getValidEntity());
        assertNull(result.getGlobalMessage());
        assertNull(result.getFieldMessages());
    }

}
