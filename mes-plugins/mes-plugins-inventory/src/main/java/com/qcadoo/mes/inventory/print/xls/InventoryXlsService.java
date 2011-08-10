package com.qcadoo.mes.inventory.print.xls;

import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;

@Service
public final class InventoryXlsService extends XlsDocumentService {

    @Override
    protected void addHeader(HSSFSheet sheet, Locale locale) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addSeries(HSSFSheet sheet, Entity entity) {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getSuffix() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getReportTitle(Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

}
