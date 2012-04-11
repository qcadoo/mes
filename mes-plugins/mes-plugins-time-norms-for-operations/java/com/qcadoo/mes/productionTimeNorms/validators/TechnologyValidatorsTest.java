package com.qcadoo.mes.productionTimeNorms.validators;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechnologyValidatorsTest {

    private TechnologyValidators technologyValidators;

    @Mock
    private Entity technology;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity operComp1, operComp2, prod1, prod2, prod1Comp, prod2Comp;

    @Mock
    private NormService normService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyValidators = new TechnologyValidators();

        // BDDMockito.
    }

    @Test
    public void shouldAlwaysPassIfTheTechnologyIsInDraftState() {
        // given
        given(technology.getStringField("state")).willReturn("01draft");

        // when
        boolean isValid = technologyValidators.checkOperationOutputQuantities(dataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public void shouldPassValidationErrorsToTheEntityForAcceptedTechnology() {
        // given
        given(technology.getStringField("state")).willReturn("02accepted");
        given(technology.getDataDefinition()).willReturn(dataDefinition);
        given(technology.getId()).willReturn(0l);
        given(dataDefinition.get(0l)).willReturn(technology);

        ReflectionTestUtils.setField(technologyValidators, "normService", normService);

        given(normService.checkOperationOutputQuantities(technology)).willReturn(asList("err1", "err2"));

        // when
        boolean isValid = technologyValidators.checkOperationOutputQuantities(dataDefinition, technology);

        // then
        assertFalse(isValid);
        verify(technology).addGlobalError("err1");
        verify(technology).addGlobalError("err2");
    }
}
