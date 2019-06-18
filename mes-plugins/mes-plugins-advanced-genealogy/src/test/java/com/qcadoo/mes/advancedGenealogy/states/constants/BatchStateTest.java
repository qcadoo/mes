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

import static com.qcadoo.mes.advancedGenealogy.states.constants.BatchState.BLOCKED;
import static com.qcadoo.mes.advancedGenealogy.states.constants.BatchState.TRACKED;
import junit.framework.Assert;

import org.junit.Test;

public class BatchStateTest {

    @Test
    public final void shouldReturnTrackedStringValue() throws Exception {
        // when
        String stringValue = TRACKED.getStringValue();

        // then
        Assert.assertEquals("01tracked", stringValue);
    }

    @Test
    public final void shouldReturnBlockedStringValue() throws Exception {
        // when
        String stringValue = BLOCKED.getStringValue();

        // then
        Assert.assertEquals("02blocked", stringValue);
    }

    @Test
    public final void shouldParseStringAndReturnTracked() throws Exception {
        // when
        BatchState state = BatchState.parseString("01tracked");

        // then
        Assert.assertEquals(TRACKED, state);
    }

    @Test
    public final void shouldParseStringAndReturnBLOCKED() throws Exception {
        // when
        BatchState state = BatchState.parseString("02blocked");

        // then
        Assert.assertEquals(BLOCKED, state);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void shouldThrowExceptionIfGivenStringValueIsNull() throws Exception {
        // when
        BatchState.parseString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void shouldThrowExceptionIfGivenStringValueIsIllegal() throws Exception {
        // when
        BatchState.parseString("00wrongState");
    }

}
