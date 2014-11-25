package com.qcadoo.mes.technologies.dataProvider;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import com.qcadoo.model.api.Entity;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

public class TechnologyDataProviderImplTest {

    private TechnologyDataProvider technologyDataProvider;

    @Mock
    private DataDefinition technologyDataDef;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyDataProvider = new TechnologyDataProviderImpl();

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        given(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY))
                .willReturn(technologyDataDef);

        ReflectionTestUtils.setField(technologyDataProvider, "dataDefinitionService", dataDefinitionService);
    }

    private void stubDataDefGetResult(final Entity entity) {
        given(technologyDataDef.get(anyLong())).willReturn(entity);
    }

    @Test
    public final void shouldReturnTechnology() {
        // given
        Entity technologyFromDb = mockEntity();
        stubDataDefGetResult(technologyFromDb);

        // when
        Optional<Entity> res = technologyDataProvider.tryFind(1L);

        // then
        Assert.assertEquals(Optional.of(technologyFromDb), res);
    }

    @Test
    public final void shouldReturnEmptyIfIdIsMissing() {
        // when
        Optional<Entity> res = technologyDataProvider.tryFind(null);

        // then
        Assert.assertEquals(Optional.<Entity> empty(), res);
    }

    @Test
    public final void shouldReturnEmptyIfEntityCannotBeFound() {
        // given
        stubDataDefGetResult(null);

        // when
        Optional<Entity> res = technologyDataProvider.tryFind(1L);

        // then
        Assert.assertEquals(Optional.<Entity> empty(), res);
    }

}
