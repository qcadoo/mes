package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.TimeUsageReportFilterFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TimeUsageReportFilterHooks {

    public void onSave(final DataDefinition timeUsageFilterDD, final Entity timeUsageFilter) {
        String selected = timeUsageFilter.getStringField(TimeUsageReportFilterFields.WORKERS_SELECTION);
        if ("01all".equals(selected)) {
            timeUsageFilter.setField(TimeUsageReportFilterFields.WORKERS, null);
        }
    }
}
