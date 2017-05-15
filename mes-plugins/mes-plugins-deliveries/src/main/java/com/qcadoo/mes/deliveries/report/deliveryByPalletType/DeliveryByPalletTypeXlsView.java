package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import com.qcadoo.report.api.xls.abstractview.AbstractXLSXView;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
class DeliveryByPalletTypeXlsView extends AbstractXLSXView {

    @Autowired
    private DeliveryByPalletTypeXlsService deliveryByPalletTypeXlsService;

    @Override
    protected void buildExcelDocument(Map<String, Object> model, XSSFWorkbook workbook, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        XSSFSheet sheet = workbook.createSheet(deliveryByPalletTypeXlsService.getReportTitle(LocaleContextHolder.getLocale()));
        deliveryByPalletTypeXlsService.buildExcelContent(workbook,sheet,model, LocaleContextHolder.getLocale());
    }
}
