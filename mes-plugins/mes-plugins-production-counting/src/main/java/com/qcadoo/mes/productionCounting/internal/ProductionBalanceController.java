package com.qcadoo.mes.productionCounting.internal;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportUtil;
import com.qcadoo.report.api.pdf.PdfUtil;

@Controller
public class ProductionBalanceController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "productionCounting/productionBalance.pdf", method = RequestMethod.GET)
    public void productionBalancePdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE);
        Entity productionBalance = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName((Date) productionBalance.getField("date"),
                translationService.translate("productionCounting.productionBalance.report.fileName", locale), "",
                PdfUtil.PDF_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(productionBalance.getStringField("fileName") + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

}
