/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.technologies.states;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;

@Service
public class TechnologyStateBeforeChangeNotifierService {

    private Set<BeforeStateChangeListener> listeners = Sets.newLinkedHashSet();

    public final boolean fireListeners(final ComponentState component, final Entity technology, final TechnologyState newState) {
        for (BeforeStateChangeListener listener : listeners) {
            if (!listener.canChange(component, technology, newState)) {
                return false;
            }
        }
        return true;
    }

    public interface BeforeStateChangeListener {

        boolean canChange(final ComponentState gridOrForm, final Entity technology, final TechnologyState newState);
    }

    public final void registerListener(final BeforeStateChangeListener beforeListener) {
        listeners.add(beforeListener);
    }

    public final void unregisterListener(final BeforeStateChangeListener beforeListener) {
        listeners.remove(beforeListener);
    }
}
