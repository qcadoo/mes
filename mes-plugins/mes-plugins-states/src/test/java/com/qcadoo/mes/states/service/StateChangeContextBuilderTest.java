package com.qcadoo.mes.states.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.MockStateChangeDescriber;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateChangeTest;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.TestState;
import com.qcadoo.mes.states.exception.AnotherChangeInProgressException;
import com.qcadoo.mes.states.exception.StateTransitionNotAlloweException;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

public class StateChangeContextBuilderTest extends StateChangeTest {

    private StateChangeContextBuilder stateChangeContextBuilder;

    private StateChangeEntityDescriber describer;

    @Mock
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private MessageService messageService;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    private class TestDescriber extends AbstractStateChangeDescriber {

        private StateChangeEntityDescriber describer = new MockStateChangeDescriber(stateChangeDD);

        @Override
        public DataDefinition getDataDefinition() {
            return describer.getDataDefinition();
        }

        @Override
        public StateEnum parseStateEnum(final String stringValue) {
            return TestState.parseString(stringValue);
        }

        @Override
        public DataDefinition getOwnerDataDefinition() {
            return ownerDD;
        }

        @Override
        public String getOwnerFieldName() {
            return "owner";
        }

    }

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        stateChangeContextBuilder = new StateChangeContextBuilderImpl();
        ReflectionTestUtils.setField(stateChangeContextBuilder, "stateChangeEntityBuilder", stateChangeEntityBuilder);
        ReflectionTestUtils.setField(stateChangeContextBuilder, "messageService", messageService);

        describer = new TestDescriber();
        stubStateChangeEntity(describer);
        stubOwner();

        given(
                stateChangeEntityBuilder.buildInitial(Mockito.eq(describer), Mockito.any(Entity.class),
                        Mockito.any(StateEnum.class))).willReturn(stateChangeEntity);
        given(stateChangeEntityBuilder.build(Mockito.eq(describer), Mockito.eq(owner), Mockito.any(StateEnum.class))).willReturn(
                stateChangeEntity);

        stubBelongsToField(stateChangeEntity, describer.getOwnerFieldName(), owner);
        stubStringField(owner, describer.getOwnerStateFieldName(), TestState.DRAFT.getStringValue());
    }

    @Test
    public final void shouldBuildStateChangeContextBasedOnStateChangeEntity() {
        // given
        stubSearchCriteria(Lists.<Entity> newArrayList());

        // when
        final StateChangeContext stateChangeContext = stateChangeContextBuilder.build(describer, stateChangeEntity);

        // then
        assertNotNull(stateChangeContext);
        assertEquals(stateChangeEntity, stateChangeContext.getStateChangeEntity());
        assertEquals(owner, stateChangeContext.getOwner());
    }

    @Test
    public final void shouldBuildStateChangeContextFromScratches() {
        // given
        given(stateChangeDD.create()).willReturn(stateChangeEntity);
        stubSearchCriteria(Lists.<Entity> newArrayList());

        // when
        final StateChangeContext stateChangeContext = stateChangeContextBuilder.build(describer, owner,
                TestState.ACCEPTED.getStringValue());

        // then
        assertNotNull(stateChangeContext);
        assertEquals(stateChangeEntity, stateChangeContext.getStateChangeEntity());
        assertEquals(owner, stateChangeContext.getOwner());
    }

    @Test
    public final void shouldThrowExceptionIfActiveStateChangeEntityAlreadyExists() {
        // given
        final Entity existingActiveStateChangeEntity = mock(Entity.class);
        given(stateChangeDD.create()).willReturn(stateChangeEntity);
        stubSearchCriteria(Lists.<Entity> newArrayList(existingActiveStateChangeEntity));

        // when
        try {
            stateChangeContextBuilder.build(describer, owner, TestState.ACCEPTED.getStringValue());
            Assert.fail();
        } catch (AnotherChangeInProgressException e) {

        }
    }

    @Test
    public final void shouldThrowExceptionIfStateChangeTransitionIsNotPermitted() {
        // given
        given(stateChangeDD.create()).willReturn(stateChangeEntity);
        stubSearchCriteria(Lists.<Entity> newArrayList());
        stubStringField(owner, describer.getOwnerStateFieldName(), TestState.DECLINED.getStringValue());

        // when
        try {
            stateChangeContextBuilder.build(describer, owner, TestState.ACCEPTED.getStringValue());
            Assert.fail();
        } catch (StateTransitionNotAlloweException e) {

        }
    }

    private void stubSearchCriteria(final List<Entity> results) {
        given(stateChangeDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(results.size());
        given(searchResult.getEntities()).willReturn(results);
    }

}
