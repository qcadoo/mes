package com.qcadoo.mes.simpleMaterialBalance.internal.print;

import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;

@Service
public final class SimpleMaterialBalanceXlsService extends XlsDocumentService {

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale) {

    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {

    }

    @Override
    protected String getSuffix() {
        return "";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("simpleMaterialBalance.simpleMaterialBalance.report.title", locale);
    }

}
