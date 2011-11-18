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
            if (listener.canChange(component, technology, newState)) {
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
