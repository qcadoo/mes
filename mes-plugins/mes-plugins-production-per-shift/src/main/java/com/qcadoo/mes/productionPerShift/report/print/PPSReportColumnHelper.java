package com.qcadoo.mes.productionPerShift.report.print;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionPerShift.report.columns.ReportColumn;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.plugin.api.RunIfEnabled;

@Service
public class PPSReportColumnHelper {

    private static final Logger logger = LoggerFactory.getLogger(PPSReportColumnHelper.class);

    @Autowired
    private List<PPSReportColumnService> services;

    @Autowired
    private ApplicationContext applicationContext;

    public PPSReportColumnService getColumnService() {
        for (PPSReportColumnService service : services) {
            if (serviceEnabled(service)) {
                return service;
            }
        }
        throw new IllegalStateException("No active PPSReportColumnService found.");
    }

    private <M extends Object & PPSReportColumnService> boolean serviceEnabled(M service) {
        RunIfEnabled runIfEnabled = service.getClass().getAnnotation(RunIfEnabled.class);
        if (runIfEnabled == null) {
            return true;
        }
        for (String pluginIdentifier : runIfEnabled.value()) {
            if (!PluginUtils.isEnabled(pluginIdentifier)) {
                return false;
            }
        }
        return true;
    }

    public List<ReportColumn> getReportColumns() {
        try {
            PPSReportColumnService service = getColumnService();
            return service.getReportColumns();
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
        }
        return Lists.newArrayList();
    }
}
