package com.qcadoo.mes.simpleMaterialBalance.internal;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportUtil;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.xls.XlsUtil;

@Controller
public class SimpleMaterialBalanceController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "simpleMaterialBalance/simpleMaterialBalance.pdf", method = RequestMethod.GET)
    public void simpleMaterialBalancePdf(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE);
        Entity simpleMaterialBalance = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName(simpleMaterialBalance,
                translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.fileName", locale), "",
                PdfUtil.PDF_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(simpleMaterialBalance.getStringField("fileName") + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    @RequestMapping(value = "simpleMaterialBalance/simpleMaterialBalance.xls", method = RequestMethod.GET)
    public void simpleMaterialBalanceXls(@RequestParam("id") final String id, final HttpServletResponse response,
            final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE);
        Entity simpleMaterialBalance = dataDefinition.get(Long.parseLong(id));
        ReportUtil.sentTranslatedFileName(simpleMaterialBalance,
                translationService.translate("simpleMaterialBalance.simpleMaterialBalance.report.fileName", locale), "",
                XlsUtil.XLS_EXTENSION, response);
        ReportUtil.sentFileAsAttachement(simpleMaterialBalance.getStringField("fileName") + XlsUtil.XLS_EXTENSION,
                ReportUtil.XLS_CONTENT_TYPE, response);
    }

}
