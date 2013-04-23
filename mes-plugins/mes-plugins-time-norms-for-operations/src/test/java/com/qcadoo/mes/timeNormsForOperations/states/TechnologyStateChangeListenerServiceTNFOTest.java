package com.qcadoo.mes.timeNormsForOperations.states;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechnologyStateChangeListenerServiceTNFOTest {

    private TechnologyStateChangeListenerServiceTNFO technologyStateChangeListenerServiceTNFO;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity technology;

    @Mock
    private StateChangeContext stateChangeContext;

    @Mock
    private NormService normService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyStateChangeListenerServiceTNFO = new TechnologyStateChangeListenerServiceTNFO();
        ReflectionTestUtils.setField(technologyStateChangeListenerServiceTNFO, "normService", normService);
    }

    @Test
    public void shouldAlwaysPassIfTheTechnologyIsInDraftState() {
        // given
        given(stateChangeContext.getOwner()).willReturn(technology);
        given(technology.getDataDefinition()).willReturn(dataDefinition);
        given(technology.getStringField("state")).willReturn("01draft");

        // when
        boolean isValid = technologyStateChangeListenerServiceTNFO.checkOperationOutputQuantities(stateChangeContext);

        // then
        assertTrue(isValid);
    }

}
