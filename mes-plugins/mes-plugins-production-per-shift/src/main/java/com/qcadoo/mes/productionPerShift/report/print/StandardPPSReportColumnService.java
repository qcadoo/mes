package com.qcadoo.mes.productionPerShift.report.print;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.report.columns.ReportColumn;
import com.qcadoo.plugin.api.RunIfEnabled;

@Service
@RunIfEnabled(ProductionPerShiftConstants.PLUGIN_IDENTIFIER)
@Order(2)
public class StandardPPSReportColumnService implements PPSReportColumnService {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public List<ReportColumn> getReportColumns() {
        List<ReportColumn> columns = Lists.newLinkedList();
        List<String> sortedColumnNames = getSortedColumnIdentifiers();
        Map<String, ReportColumn> reportColumns = applicationContext.getBeansOfType(ReportColumn.class);
        for (String name : sortedColumnNames) {
            ReportColumn column = reportColumns.get(name);
            if (column != null) {
                columns.add(column);
            }
        }
        return columns;
    }

    private List<String> getSortedColumnIdentifiers() {
        List<String> columns = Lists.newLinkedList();
        columns.add("productionLineReportColumn");
        columns.add("productNumberReportColumn");
        columns.add("productNameReportColumn");
        columns.add("commentReportColumn");
        columns.add("orderNumberReportColumn");
        columns.add("orderSizeReportColumn");
        return columns;
    }

}
