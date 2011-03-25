/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.view.internal.states;

import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.qcadoo.view.internal.ComponentState;
import com.qcadoo.view.internal.ScopeEntityIdChangeListener;
import com.qcadoo.view.internal.components.form.FormComponentState;

public class ScopeEntityIdChangeListenerTest extends AbstractStateTest {

    @Test
    public void shouldHaveScopeListeners() throws Exception {
        // given
        ComponentState component1 = createMockComponent("component1");
        ComponentState component2 = createMockComponent("component2");

        FormComponentState container = new FormComponentState(null, null);
        container.addScopeEntityIdChangeListener("component1", (ScopeEntityIdChangeListener) component1);
        container.addScopeEntityIdChangeListener("component2", (ScopeEntityIdChangeListener) component2);

        // when
        container.setFieldValue(13L);

        // then
        verify((ScopeEntityIdChangeListener) component1).onScopeEntityIdChange(13L);
        verify((ScopeEntityIdChangeListener) component2).onScopeEntityIdChange(13L);
    }

}
