package com.qcadoo.mes.materialFlowResources.helpers;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.context.i18n.LocaleContextHolder;

public class NotEnoughResourcesErrorMessageHolder {

    private final List<String> messages;

    private final NumberService numberService;
    private final TranslationService translationService;

    private final String BATCH_MESSAGE = "materialFlow.error.position.quantity.notEnoughResources.batch";
    private final String MISSING_MESSAGE = "materialFlow.error.position.quantity.notEnoughResources.missing";

    NotEnoughResourcesErrorMessageHolder(NumberService numberService, TranslationService translationService) {
        this.messages = new ArrayList<>();
        this.numberService = numberService;
        this.translationService = translationService;
    }

    public void addErrorEntry(Entity product, Entity batch, BigDecimal quantity) {
        StringBuilder message = new StringBuilder();
        message.append("(");
        message.append(product.getStringField(ProductFields.NUMBER));
        message.append(") ");
        message.append(product.getStringField(ProductFields.NAME));
        if(Objects.nonNull(batch)) {
            message.append(", ");
            String batchNumber = batch.getStringField(BatchFields.NUMBER);
            message.append(translationService.translate(BATCH_MESSAGE, LocaleContextHolder.getLocale(), batchNumber));
        }
        message.append(". ");
        message.append(translationService.translate(MISSING_MESSAGE, LocaleContextHolder.getLocale()));
        message.append(" ");
        message.append(numberService.format(quantity));
        message.append(" ");
        message.append(product.getStringField(ProductFields.UNIT));
        messages.add(message.toString());
    }

    public List<String> getErrorMessages() {
        return messages;
    }

    @Override
    public String toString() {
        StringBuilder errorMessage = new StringBuilder();
        for (String message : messages) {
            if (errorMessage.length() > 0) {
                errorMessage.append(", ");
            }
            errorMessage.append(message);
        }
        return errorMessage.toString();
    }
}
