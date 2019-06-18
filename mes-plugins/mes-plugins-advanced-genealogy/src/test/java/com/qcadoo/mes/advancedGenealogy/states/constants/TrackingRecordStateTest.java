/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.advancedGenealogy.states.constants;

import static com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState.ACCEPTED;
import static com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState.DECLINED;
import static com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState.DRAFT;

import org.junit.Assert;
import org.junit.Test;

public class TrackingRecordStateTest {

    @Test
    public final void shouldReturnDraftStringValue() throws Exception {
        // when
        String stringValue = DRAFT.getStringValue();

        // then
        Assert.assertEquals("01draft", stringValue);
    }

    @Test
    public final void shouldReturnAcceptedStringValue() throws Exception {
        // when
        String stringValue = ACCEPTED.getStringValue();

        // then
        Assert.assertEquals("02accepted", stringValue);
    }

    @Test
    public final void shouldReturnDeclinedStringValue() throws Exception {
        // when
        String stringValue = DECLINED.getStringValue();

        // then
        Assert.assertEquals("03declined", stringValue);
    }

    @Test
    public final void shouldParseStringAndReturnDraft() throws Exception {
        // when
        TrackingRecordState state = TrackingRecordState.parseString("01draft");

        // then
        Assert.assertEquals(DRAFT, state);
    }

    @Test
    public final void shouldParseStringAndReturnAccepted() throws Exception {
        // when
        TrackingRecordState state = TrackingRecordState.parseString("02accepted");

        // then
        Assert.assertEquals(ACCEPTED, state);
    }

    @Test
    public final void shouldParseStringAndReturnDeclined() throws Exception {
        // when
        TrackingRecordState state = TrackingRecordState.parseString("03declined");

        // then
        Assert.assertEquals(DECLINED, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void shouldThrowExceptionIfGivenStringValueIsNull() throws Exception {
        // when
        TrackingRecordState.parseString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void shouldThrowExceptionIfGivenStringValueIsIllegal() throws Exception {
        // when
        TrackingRecordState.parseString("00wrongState");
    }

    @Test
    public final void shouldSwitchFromDraftToAccepted() throws Exception {
        // when
        final boolean canChange = DRAFT.canChangeTo(ACCEPTED);

        // then
        Assert.assertTrue(canChange);
    }

    @Test
    public final void shouldSwitchFromDraftToDeclined() throws Exception {
        // when
        final boolean canChange = DRAFT.canChangeTo(DECLINED);

        // then
        Assert.assertTrue(canChange);
    }

    @Test
    public final void shouldSwitchFromAcceptedToDeclined() throws Exception {
        // when
        final boolean canChange = ACCEPTED.canChangeTo(DECLINED);

        // then
        Assert.assertTrue(canChange);
    }

    @Test
    public final void shouldNotSwitchFromAcceptedToDraft() throws Exception {
        // when
        final boolean canChange = ACCEPTED.canChangeTo(DRAFT);

        // then
        Assert.assertFalse(canChange);
    }

    @Test
    public final void shouldNotSwitchFromDeclinedToDraft() throws Exception {
        // when
        final boolean canChange = DECLINED.canChangeTo(DRAFT);

        // then
        Assert.assertFalse(canChange);
    }

    @Test
    public final void shouldNotSwitchFromDeclinedToAccepted() throws Exception {
        // when
        final boolean canChange = DECLINED.canChangeTo(ACCEPTED);

        // then
        Assert.assertFalse(canChange);
    }

}
