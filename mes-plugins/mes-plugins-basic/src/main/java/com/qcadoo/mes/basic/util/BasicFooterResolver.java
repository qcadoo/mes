package com.qcadoo.mes.basic.util;

import static com.qcadoo.mes.basic.constants.ParameterFields.ADDITIONAL_TEXT_IN_FOOTER;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.Footer;
import com.qcadoo.report.api.FooterResolver;
import com.qcadoo.security.api.SecurityService;

@Component
public class BasicFooterResolver implements FooterResolver {

    private static final String EMAIL = "email";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CompanyService companyService;

    @Override
    public Footer resolveFooter(final Locale locale) {
        String companyName = "";
        String address = "";
        String phoneEmail = "";

        Entity parameter = parameterService.getParameter();
        Entity company = companyService.getCompany();
        String additionalText = parameter.getStringField(ADDITIONAL_TEXT_IN_FOOTER);
        if (additionalText == null) {
            additionalText = "";
        }

        StringBuilder generatedBy = new StringBuilder();
        generatedBy = generatedBy.append(translationService.translate("qcadooReport.commons.generatedBy.label", locale));
        generatedBy = generatedBy.append(" ");
        generatedBy = generatedBy.append(securityService.getCurrentUserName());

        if (company != null) {

            StringBuilder companyData = new StringBuilder();

            companyData = companyData.append(company.getStringField("name"));
            if (company.getStringField("tax") != null) {
                companyData = companyData.append(", ");
                companyData = companyData.append(translationService.translate("qcadooReport.commons.tax.label", locale) + ": ");
                companyData = companyData.append(company.getStringField("tax"));
            }
            companyName = companyData.toString();

            if (company.getStringField("street") != null && company.getStringField("house") != null
                    && company.getStringField("zipCode") != null && company.getStringField("city") != null) {
                companyData.setLength(0);
                companyData = companyData.append(company.getStringField("street"));
                companyData = companyData.append(" ");
                companyData = companyData.append(company.getStringField("house"));
                if (company.getStringField("flat") != null) {
                    companyData = companyData.append("/");
                    companyData = companyData.append(company.getStringField("flat"));
                }
                companyData = companyData.append(", ");
                companyData = companyData.append(company.getStringField("zipCode"));
                companyData = companyData.append(" ");
                companyData = companyData.append(company.getStringField("city"));
                if (company.getStringField("country") != null) {
                    companyData = companyData.append(", ");
                    companyData = companyData.append(company.getStringField("country"));
                }
                address = companyData.toString();
            }

            if (company.getStringField("phone") == null) {
                if (company.getStringField(EMAIL) != null) {
                    companyData.setLength(0);
                    companyData = companyData.append("E-mail: ");
                    companyData = companyData.append(company.getStringField(EMAIL));
                    phoneEmail = companyData.toString();
                }
            } else {
                companyData.setLength(0);
                companyData = companyData.append(translationService.translate("qcadooReport.commons.phone.label", locale));
                companyData = companyData.append(": ");
                companyData = companyData.append(company.getStringField("phone"));
                if (company.getStringField(EMAIL) != null) {
                    companyData = companyData.append(", ");
                    companyData = companyData.append("E-mail: ");
                    companyData = companyData.append(company.getStringField(EMAIL));
                }
                phoneEmail = companyData.toString();
            }
        }

        return new Footer(translationService.translate("qcadooReport.commons.page.label", locale), translationService.translate(
                "qcadooReport.commons.of.label", locale), companyName, address, phoneEmail, generatedBy.toString(),
                additionalText);
    }
}
