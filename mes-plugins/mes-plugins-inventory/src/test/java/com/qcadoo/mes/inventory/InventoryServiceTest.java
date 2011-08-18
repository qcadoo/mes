package com.qcadoo.mes.inventory;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class InventoryServiceTest {

    private InventoryService inventoryService;

    private Entity entity;

    @Autowired
    private DataDefinition dataDefinition;

    @Before
    public void init() {
        inventoryService = new InventoryService();
        entity = mock(Entity.class);
    }

    @Test
    public void shouldClearGeneratedOnCopy() throws Exception {
        // given

        // when
        boolean bool = inventoryService.clearGeneratedOnCopy(dataDefinition, entity);
        // then
        assertTrue(bool);
        verify(entity, Mockito.times(3)).setField(Mockito.anyString(), Mockito.any());

    }
}
