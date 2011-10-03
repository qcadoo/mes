package com.qcadoo.mes.materialFlow;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class MaterialFlowServiceTest {

    private MaterialsInStockAreasService materialFlowService;

    private Entity entity;

    @Autowired
    private DataDefinition dataDefinition;

    @Before
    public void init() {
        materialFlowService = new MaterialsInStockAreasService();
        entity = mock(Entity.class);
    }

    @Test
    public void shouldClearGeneratedOnCopy() throws Exception {
        // given

        // when
        boolean bool = materialFlowService.clearGeneratedOnCopy(dataDefinition, entity);
        // then
        assertTrue(bool);
        verify(entity, Mockito.times(4)).setField(Mockito.anyString(), Mockito.any());

    }
}
