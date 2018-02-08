package com.qcadoo.mes.basic;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TimetableExceptionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> findForLineAndShift(Entity productionLine, Entity shift) {
        List<Entity> exceptions = Lists.newArrayList();

        StringBuilder query = new StringBuilder();
        query.append("SELECT timetableException FROM #basic_shiftTimetableException timetableException ");
        query.append("JOIN timetableException.productionLines productionLine ");
        query.append("JOIN timetableException.shifts shift ");
        query.append("WHERE productionLine.id = :productionLine AND shift.id = :shift");

        return getDD().find(query.toString())
                .setLong("productionLine", productionLine.getId())
                .setLong("shift", shift.getId())
                .list().getEntities();
    }

    private DataDefinition getDD(){
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.SHIFT_TIMETABLE_EXCEPTION);
    }

}
