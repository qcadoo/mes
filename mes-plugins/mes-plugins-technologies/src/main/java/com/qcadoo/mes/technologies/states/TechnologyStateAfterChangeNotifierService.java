/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
public class TechnologyStateAfterChangeNotifierService {

    private Set<AfterStateChangeListener> listeners = Sets.newLinkedHashSet();

    public final void fireListeners(final ComponentState component, final Entity technology, final TechnologyState newState) {
        for (AfterStateChangeListener listener : listeners) {
            listener.wasChanged(component, technology, newState);
        }
    }

    public interface AfterStateChangeListener {

        void wasChanged(final ComponentState gridOrForm, final Entity technology, final TechnologyState newState);
    }

    public final void registerListener(final AfterStateChangeListener beforeListener) {
        listeners.add(beforeListener);
    }

    public final void unregisterListener(final AfterStateChangeListener beforeListener) {
        listeners.remove(beforeListener);
    }
}
