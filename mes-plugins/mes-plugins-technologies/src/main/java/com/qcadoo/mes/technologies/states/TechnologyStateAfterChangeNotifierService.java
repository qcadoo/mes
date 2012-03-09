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
