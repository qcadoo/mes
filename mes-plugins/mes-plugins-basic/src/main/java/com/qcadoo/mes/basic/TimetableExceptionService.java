package com.qcadoo.mes.basic;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;

@Service
class TimetableExceptionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    List<Entity> findFor(final Entity productionLine, final Entity shift, final Date date) {
        return findFor(Lists.newArrayList(productionLine.getId()), Lists.newArrayList(shift.getId()), date, null);
    }

    List<Entity> findFor(final Entity productionLine, final Entity shift, final Date date, final String type) {
        return findFor(Lists.newArrayList(productionLine.getId()), Lists.newArrayList(shift.getId()), date, type);
    }

    private List<Entity> findFor(final List<Long> productionLineIds, final List<Long> shiftIds, final Date date,
            final String type) {
        List<Entity> shiftTimetableExceptions = Lists.newArrayList();

        if (!productionLineIds.isEmpty() && !shiftIds.isEmpty()) {
            StringBuilder query = new StringBuilder();

            query.append("SELECT timetableException FROM #basic_shiftTimetableException timetableException");
            query.append(" JOIN timetableException.productionLines productionLine");
            query.append(" JOIN timetableException.shifts shift");
            query.append(" WHERE productionLine.id IN (:productionLines)");
            query.append(" AND shift.id IN (:shifts)");

            if (date != null) {
                query.append(
                        " AND to_char(timetableException.fromDate,'yyyy-MM-dd') <= :date AND to_char(timetableException.toDate,'yyyy-MM-dd') >= :date");
            }

            if (StringUtils.isNotEmpty(type)) {
                query.append(" AND type = :type");
            }

            SearchQueryBuilder searchQueryBuilder = getShiftTimetableExceptionDD().find(query.toString())
                    .setParameterList("productionLines", productionLineIds).setParameterList("shifts", shiftIds);

            if (date != null) {
                searchQueryBuilder.setParameter("date", DateUtils.toDateString(date));
            }

            if (StringUtils.isNotEmpty(type)) {
                searchQueryBuilder.setString("type", type);
            }

            shiftTimetableExceptions = searchQueryBuilder.list().getEntities();
        }

        return shiftTimetableExceptions;
    }

    private DataDefinition getShiftTimetableExceptionDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT_TIMETABLE_EXCEPTION);
    }

}
