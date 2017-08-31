package com.qcadoo.mes.materialFlowResources.helpers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

public class NotEnoughResourcesErrorMessageHolder {

    private final List<String> messages;

    private final NumberService numberService;

    NotEnoughResourcesErrorMessageHolder(NumberService numberService) {
        this.messages = new ArrayList<>();
        this.numberService = numberService;
    }

    public void addErrorEntry(Entity product, BigDecimal quantity) {
        StringBuilder message = new StringBuilder();
        message.append("(");
        message.append(product.getStringField(ProductFields.NUMBER));
        message.append(") ");
        message.append(product.getStringField(ProductFields.NAME));
        message.append(" - ");
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
