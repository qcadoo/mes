package com.qcadoo.mes.technologies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.technologies.listeners.TechnologyStateChangeListener;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeNotifierService;
import com.qcadoo.plugin.api.Module;

@Component
public class TechnologyStartupModule extends Module {
    
    @Autowired
    private TechnologyStateChangeNotifierService technologyStateChangeNotifierService;
    
    @Autowired
    private TechnologyStateChangeListener technologyStateChangeListener;
    
    @Override
    public void enableOnStartup() {
        technologyStateChangeNotifierService.registerListener(technologyStateChangeListener);
    }

    @Override
    public void disableOnStartup() {
        technologyStateChangeNotifierService.unregisterListener(technologyStateChangeListener);
    }
}
