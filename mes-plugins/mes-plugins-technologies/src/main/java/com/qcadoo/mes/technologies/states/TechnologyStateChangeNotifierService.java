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
