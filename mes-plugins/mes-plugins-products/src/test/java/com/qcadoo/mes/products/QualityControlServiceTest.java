package com.qcadoo.mes.products;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.api.DataDefinitionService;

public class QualityControlServiceTest {

    private QualityControlService qualityControlService;

    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        qualityControlService = new QualityControlService();
        setField(qualityControlService, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldSetRequiredOnComment() throws Exception {
        // given

        // when
        // then
    }

}
