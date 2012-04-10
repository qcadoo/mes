package com.qcadoo.mes.productionTimeNorms.hooks;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyOperationComponentViewHooksTN {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyService technologyService;

    public void checkOperationOutputQuantities(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");

        Entity operationComponent = form.getEntity();
        operationComponent = operationComponent.getDataDefinition().get(operationComponent.getId());

        BigDecimal timeNormsQuantity = operationComponent.getDecimalField("productionInOneCycle");

        Entity productOutComponent = null;

        try {
            productOutComponent = technologyService.getMainOutputProductComponent(operationComponent);
        } catch (IllegalStateException e) {
            return;
        }

        Locale locale = LocaleContextHolder.getLocale();

        BigDecimal currentQuantity = productOutComponent.getDecimalField("quantity");

        if (timeNormsQuantity.compareTo(currentQuantity) != 0) { // Not using equals intentionally
            StringBuilder message = new StringBuilder();
            message.append(translationService.translate(
                    "technologies.technologyOperationComponent.validate.error.invalidQuantity1", locale));
            message.append(" ");
            message.append(currentQuantity.toString());
            message.append(" ");
            message.append(productOutComponent.getBelongsToField("product").getStringField("unit"));
            message.append(" ");
            message.append(translationService.translate(
                    "technologies.technologyOperationComponent.validate.error.invalidQuantity2", locale));

            ComponentState productionInOneCycle = view.getComponentByReference("productionInOneCycle");

            productionInOneCycle.addMessage(message.toString(), MessageType.FAILURE);
        }
    }
}
