package com.qcadoo.mes.productionPerShift.report.print;

import java.util.List;

import com.qcadoo.mes.productionPerShift.report.columns.ReportColumn;

public interface PPSReportColumnService {

    List<ReportColumn> getReportColumns();
}
