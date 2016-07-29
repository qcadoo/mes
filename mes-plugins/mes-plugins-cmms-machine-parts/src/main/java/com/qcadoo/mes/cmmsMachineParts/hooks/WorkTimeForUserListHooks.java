package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class WorkTimeForUserListHooks {

    @Autowired
    private UserService userService;

    public void fillDefaultFilters(final ViewDefinitionState view) {
        CheckBoxComponent initialized = (CheckBoxComponent) view.getComponentByReference("initialized");
        if (initialized.isChecked()) {
            return;
        }
        initialized.setChecked(true);
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        Entity currentUser = userService.getCurrentUserEntity();

        Map<String, String> filters = Maps.newHashMap();
        DateTime currentDate = getCurrentDate();
        filters.put("startDate", ">=" + currentDate.toString("yyyy-MM-dd hh:mm:ss"));
        filters.put("finishDate", "=<" + currentDate.plusDays(1).toString("yyyy-MM-dd hh:mm:ss"));
        filters.put("username", currentUser.getStringField("userName"));
        grid.setFilters(filters);
    }

    private DateTime getCurrentDate() {
        DateTime now = DateTime.now();
        DateTime firstShiftStart = DateTime.now().withMillisOfDay(21600000);
        DateTimeComparator timeComparator = DateTimeComparator.getTimeOnlyInstance();
        if (timeComparator.compare(firstShiftStart, now) > 0) {
            return firstShiftStart.minusDays(1);
        }
        return firstShiftStart;
    }
}
