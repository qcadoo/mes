package com.qcadoo.mes.cmmsMachineParts.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public class MaintenanceEventStateChangeListenerService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

}
