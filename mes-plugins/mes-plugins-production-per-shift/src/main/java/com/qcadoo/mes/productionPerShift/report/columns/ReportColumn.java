package com.qcadoo.mes.productionPerShift.report.columns;

import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;

import com.qcadoo.mes.productionPerShift.report.print.PPSReportXlsStyleContainer;
import com.qcadoo.model.api.Entity;

public interface ReportColumn {

    String getIdentifier();

    String getHeader(final Locale locale);

    Object getValue(final Entity productionPerShift);

    Object getFirstRowValue(final Entity productionPerShift);

    String getFirstRowChangeoverValue(final Entity productionPerShift);

    String getChangeoverValue(final Entity productionPerShift);

    int getColumnWidth();

    void setGreyDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer);

    void setGreyDataStyleEnd(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer);

    void setWhiteDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer);

    void setWhiteDataStyleEnd(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer);

    void setHeaderStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer);

}
