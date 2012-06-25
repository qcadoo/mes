package com.qcadoo.mes.states;

import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.FieldDefinition;

public class AbstractStateChangeDescriberTest {

    private static DataDefinition dataDefinition;

    private TestStateChangeDescriber testStateChangeDescriber;

    @Mock
    private Map<String, FieldDefinition> fieldsMap;

    @Mock
    private StateEnum stateEnum;

    private Set<String> dataDefFieldsSet;

    private class TestStateChangeDescriber extends AbstractStateChangeDescriber {

        @Override
        public DataDefinition getDataDefinition() {
            return dataDefinition;
        }

        @Override
        public StateEnum parseStateEnum(final String stringValue) {
            return stateEnum;
        }

        @Override
        public DataDefinition getOwnerDataDefinition() {
            return mock(DataDefinition.class);
        }

    }

    private class BrokenTestStateChangeDescriber extends AbstractStateChangeDescriber {

        @Override
        public DataDefinition getDataDefinition() {
            return dataDefinition;
        }

        @Override
        public StateEnum parseStateEnum(final String stringValue) {
            return stateEnum;
        }

        @Override
        public String getSourceStateFieldName() {
            return "state";
        }

        @Override
        public String getTargetStateFieldName() {
            return "state";
        }

        @Override
        public DataDefinition getOwnerDataDefinition() {
            return mock(DataDefinition.class);
        }

    }

    @BeforeClass
    public static void initClass() {
        dataDefinition = mock(DataDefinition.class);
    }

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        testStateChangeDescriber = new TestStateChangeDescriber();
        dataDefFieldsSet = Sets.newHashSet("sourceState", "targetState", "status", "messages", "owner", "phase", "shift",
                "worker", "dateAndTime");

        given(dataDefinition.getFields()).willReturn(fieldsMap);
        given(fieldsMap.keySet()).willReturn(dataDefFieldsSet);
    }

    @Test
    public final void shouldCheckFieldsPass() {
        try {
            testStateChangeDescriber.checkFields();
        } catch (IllegalStateException e) {
            fail();
        }
    }

    @Test(expected = IllegalStateException.class)
    public final void shouldCheckFieldsThrowExceptionIfAtLeastOneFieldIsMissing() {
        // given
        dataDefFieldsSet.remove("sourceState");

        // when
        testStateChangeDescriber.checkFields();
    }

    @Test(expected = IllegalStateException.class)
    public final void shouldCheckFieldsThrowExceptionIfAtLeastOneFieldNameIsNotUnique() {
        // given
        BrokenTestStateChangeDescriber brokenTestStateChangeDescriber = new BrokenTestStateChangeDescriber();

        // when
        brokenTestStateChangeDescriber.checkFields();
    }

}
