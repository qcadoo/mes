package com.qcadoo.mes.orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.orders.listeners.TechnologyStateListeners;
import com.qcadoo.mes.technologies.states.TechnologyStateAfterChangeNotifierService;
import com.qcadoo.plugin.api.Module;

@Component
public class TechnologyOnStartupModule extends Module {

    @Autowired
    private TechnologyStateAfterChangeNotifierService technologyStateAfterChangeNotifierService;

    @Autowired
    private TechnologyStateListeners technologyStateListener;

    @Override
    public void enable() {
        registerListeners();
    }

    @Override
    public void enableOnStartup() {
        registerListeners();
    }

    private void registerListeners() {
        technologyStateAfterChangeNotifierService.registerListener(technologyStateListener);
    }

    @Override
    public void disable() {
        technologyStateAfterChangeNotifierService.unregisterListener(technologyStateListener);
    }
}
