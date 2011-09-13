package com.qcadoo.mes.costCalculation.print;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.costCalculation.constants.CostCalculateConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportUtil;
import com.qcadoo.report.api.pdf.PdfUtil;

@Controller
public class CostCalculationController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "costCalculation/costCalculation.pdf", method = RequestMethod.GET)
    public void costCalculationPdf(@RequestParam("id") final String id, final HttpServletResponse response, final Locale locale) {
        DataDefinition dataDefinition = dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER,
                CostCalculateConstants.MODEL_COST_CALCULATION);
        Entity costCalculation = dataDefinition.get(Long.parseLong(id));
        sentTranslatedFileName(costCalculation,
                translationService.translate("costCalculation.costCalculation.report.fileName", locale), "",
                PdfUtil.PDF_EXTENSION, response, "dateOfCalculation");
        ReportUtil.sentFileAsAttachement(costCalculation.getStringField("fileName") + PdfUtil.PDF_EXTENSION,
                ReportUtil.PDF_CONTENT_TYPE, response);
    }

    public static void sentTranslatedFileName(final Entity entity, final String fileName, final String suffix,
            final String extension, final HttpServletResponse response, final String dateFieldName) {
        Object date = entity.getField(dateFieldName);
        String translatedFileName = fileName + "_" + PdfUtil.D_T_F.format((Date) date) + "_" + suffix + extension;
        response.setHeader("Content-disposition", "attachment; filename=" + translatedFileName);
    }
}
