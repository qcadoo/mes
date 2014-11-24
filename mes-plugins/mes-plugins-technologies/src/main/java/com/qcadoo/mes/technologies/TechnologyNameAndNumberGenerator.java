package com.qcadoo.mes.technologies;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TechnologyNameAndNumberGenerator {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    public String generateNumber(final Entity product) {
        String numberPrefix = product.getField(ProductFields.NUMBER) + "-";
        return numberGeneratorService.generateNumberWithPrefix(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY, 3, numberPrefix);
    }

    public String generateName(final Entity product) {
        LocalDate date = LocalDate.now();
        String currentDateString = String.format("%s.%s", date.getYear(), date.getMonthValue());
        String productName = product.getStringField(ProductFields.NAME);
        String productNumber = product.getStringField(ProductFields.NUMBER);
        return translationService.translate("technologies.operation.name.default", LocaleContextHolder.getLocale(), productName,
                productNumber, currentDateString);
    }
}
