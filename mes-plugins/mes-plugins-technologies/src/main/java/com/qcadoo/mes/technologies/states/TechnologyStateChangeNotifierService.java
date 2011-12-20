/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyStateChangeNotifierService {

    private Set<StateChangeListener> listeners = Sets.newLinkedHashSet();

    public final List<MessageHolder> onTechnologyStateChange(final Entity technology, final TechnologyState newState) {
        List<MessageHolder> validationResulsts = Lists.newArrayList();
        for (StateChangeListener listener : listeners) {
            validationResulsts.addAll(listener.onStateChange(technology, newState));
        }
        return validationResulsts;
    }

    public interface StateChangeListener {

        List<MessageHolder> onStateChange(final Entity technology, final TechnologyState newState);
    }

    public final void registerListener(final StateChangeListener validationListener) {
        listeners.add(validationListener);
    }

    public final void unregisterListener(final StateChangeListener validationListener) {
        listeners.remove(validationListener);
    }

}
