package com.qcadoo.mes.technologies.logging;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;

@Service
public class TechnologyLoggingService {
    
    @Autowired
    private DataDefinitionService dataDefinitionService;
    
    @Autowired
    private SecurityService securityService;
    
    @Autowired
    private ShiftsService shiftService;
    
    public void logStateChange(final Entity technology, final TechnologyState oldState, final TechnologyState newState) {
        final Date logDate = new Date();
        final Entity logEntity = createStateLog();
      
        logEntity.setField("previousState", oldState.getStringValue());
        logEntity.setField("currentState", newState.getStringValue());
        logEntity.setField("worker", securityService.getCurrentUserName());
        logEntity.setField("shift", shiftService.getShiftFromDate(logDate));
        logEntity.setField("dateAndTime", logDate);
        
        saveStateLog(technology, logEntity);
    }
    
    private Entity createStateLog() {
        return getLoggingDataDefinition().create();
    }
    
    private void saveStateLog(final Entity technology, final Entity logEntity) {
        logEntity.setField("technology", technology);
        logEntity.getDataDefinition().save(logEntity);
    }
    
    private DataDefinition getLoggingDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_LOGGING);
    }
}
