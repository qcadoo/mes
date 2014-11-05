package com.qcadoo.mes.materialFlowResources.listeners;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ResourceDetailsListeners {

    @Autowired
    ResourceCorrectionService resourceCorrectionService;

    private static final String L_FORM = "form";

    public void createResourceCorrection(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent resourceForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent quantityInput = (FieldComponent) view.getComponentByReference(ResourceFields.QUANTITY);
        String newQuantity = (String) quantityInput.getFieldValue();

        Either<Exception, Optional<BigDecimal>> quantity = BigDecimalUtils.tryParse(newQuantity, view.getLocale());

        if (quantity.isRight() && quantity.getRight().isPresent()) {
            if (!validateDecimal(newQuantity, view.getLocale())) {
                char separator = ((DecimalFormat) DecimalFormat.getInstance(view.getLocale())).getDecimalFormatSymbols()
                        .getDecimalSeparator();
                quantityInput.addMessage("materialFlowResources.error.invalidSeparator", MessageType.FAILURE, separator + "");
                return;

            }
            Entity resource = resourceForm.getPersistedEntityWithIncludedFormValues();
            BigDecimal correctQuantity = quantity.getRight().get();
            if (correctQuantity.compareTo(BigDecimal.ZERO) > 0) {
                boolean corrected = resourceCorrectionService.createCorrectionForResource(resource.getId(), correctQuantity);
                if (!corrected) {
                    resourceForm.addMessage("materialFlow.info.correction.resourceNotChanged", MessageType.INFO);
                } else {

                    resource.setField(ResourceFields.IS_CORRECTED, true);
                    resource.getDataDefinition().save(resource);
                    resourceForm.performEvent(view, "reset");
                    quantityInput.requestComponentUpdateState();
                    resourceForm.addMessage("materialFlow.success.correction.correctionCreated", MessageType.SUCCESS);
                }
            } else {
                quantityInput.addMessage("materialFlow.error.correction.invalidQuantity", MessageType.FAILURE);
            }
        } else {
            quantityInput.addMessage("materialFlow.error.correction.invalidQuantity", MessageType.FAILURE);
        }

    }

    // FIXME for english locale should also prevent user from using "," as decimal separator, not grouping separator
    private boolean validateDecimal(final String decimal, final Locale locale) {
        String trimedValue = StringUtils.trim(decimal);
        ParsePosition parsePosition = new ParsePosition(0);
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        formatter.setParseBigDecimal(true);
        Object parsedValue = formatter.parseObject(trimedValue, parsePosition);
        if (parsePosition.getIndex() != (trimedValue.length())) {
            return false;
        }
        return true;
    }
}
