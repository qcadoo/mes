/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.states;

import static org.junit.Assert.assertTrue;
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

        @Override
        public String getOwnerFieldName() {
            return "owner";
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

        @Override
        public String getOwnerFieldName() {
            return "owner";
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

    @Test
    public final void shouldCheckFieldsThrowExceptionIfAtLeastOneFieldIsMissing() {
        // given
        dataDefFieldsSet.remove("sourceState");

        try {
            // when
            testStateChangeDescriber.checkFields();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }

    }

    @Test
    public final void shouldCheckFieldsThrowExceptionIfAtLeastOneFieldNameIsNotUnique() {
        // given
        BrokenTestStateChangeDescriber brokenTestStateChangeDescriber = new BrokenTestStateChangeDescriber();

        try {
            // when
            brokenTestStateChangeDescriber.checkFields();
        } catch (Exception e) {
            // then
            assertTrue(e instanceof IllegalStateException);
        }
    }

}
