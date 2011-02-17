package com.qcadoo.mes.basic;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;

public class ParameterServiceTest {

    @Test
    public void shouldReturnExistingGenealogyAttributeId() throws Exception {
        // given
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new DefaultEntity("", "", 13L));
        entities.add(new DefaultEntity("", "", 14L));

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        given(dataDefinitionService.get("basic", "parameter").find().withMaxResults(1).list().getEntities()).willReturn(entities);

        ParameterService parameterService = new ParameterService();
        setField(parameterService, "dataDefinitionService", dataDefinitionService);

        // when
        Long id = parameterService.getParameterId();

        // then
        assertEquals(Long.valueOf(13L), id);
    }

    @Test
    public void shouldReturnNewGenealogyAttributeId() throws Exception {
        // given
        Entity newEntity = new DefaultEntity("basic", "parameter");
        newEntity.setField("checkDoneOrderForQuality", false);
        newEntity.setField("batchForDoneOrder", "01none");

        Entity savedEntity = new DefaultEntity("basic", "parameter", 15L);

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        given(dataDefinitionService.get("basic", "parameter").find().withMaxResults(1).list().getEntities()).willReturn(
                new ArrayList<Entity>());
        given(dataDefinitionService.get("basic", "parameter").save(newEntity)).willReturn(savedEntity);

        ParameterService parameterService = new ParameterService();
        setField(parameterService, "dataDefinitionService", dataDefinitionService);

        // when
        Long id = parameterService.getParameterId();

        // then
        verify(dataDefinitionService.get("basic", "parameter")).save(newEntity);
        assertEquals(Long.valueOf(15L), id);
    }

}
