package com.qcadoo.mes.minimalAffordableQuantity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class QuantityService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public void checkMinimalAffordableQuantity(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {

        ComponentState form = (ComponentState) viewDefinitionState.getComponentByReference("form");
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("plannedQuantity");
        Entity technology = dataDefinitionService.get("technologies", "technology").get((Long) technologyLookup.getFieldValue());

        if (technology == null || technology.getId() == null) {
            return;
        }
        Entity technologyEntity = dataDefinitionService.get("technologies", "technology").get(technology.getId());
        if (technologyEntity.getField("minimalQuantity") == null) {
            return;
        } else {
            BigDecimal plannedQuantityBigDecFormat = getBigDecimalFromField(plannedQuantity.getFieldValue(),
                    viewDefinitionState.getLocale());
            BigDecimal technologyBigDecimal = getBigDecimalFromField(technologyEntity.getField("minimalQuantity"),
                    viewDefinitionState.getLocale());

            if (plannedQuantityBigDecFormat.compareTo(technologyBigDecimal) < 0) {

                form.addMessage(
                        translationService.translate("orders.order.report.minimalQuantity", viewDefinitionState.getLocale()),
                        MessageType.INFO, false);
            }
        }

    }

    public BigDecimal getBigDecimalFromField(final Object value, final Locale locale) {
        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);
            return new BigDecimal(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
