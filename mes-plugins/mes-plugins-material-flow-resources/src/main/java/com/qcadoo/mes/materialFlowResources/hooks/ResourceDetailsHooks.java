package com.qcadoo.mes.materialFlowResources.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ResourceDetailsHooks {
    
    @Autowired
    private NumberService numberService;
    
    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity resource = form.getPersistedEntityWithIncludedFormValues();
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(ResourceFields.STORAGE_LOCATION);
        FilterValueHolder filter = storageLocationLookup.getFilterValue();
        Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
        Entity warehouse = resource.getBelongsToField(ResourceFields.LOCATION);
        filter.put("product", product.getId());
        filter.put("location", warehouse.getId());
        storageLocationLookup.setFilterValue(filter);
        
        fillUnitField(view, resource);
    }
    
    public void onQuantityChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent quantityField = (FieldComponent) viewDefinitionState.getComponentByReference(ResourceFields.QUANTITY);
        FieldComponent quantityInAdditionalUnitField = (FieldComponent) viewDefinitionState.getComponentByReference(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT);
        
        Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils.tryParseAndIgnoreSeparator((String) quantityField.getFieldValue(), viewDefinitionState.getLocale());
        if (maybeQuantity.isRight() && maybeQuantity.getRight().isPresent()) {
            FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
            Entity resource = form.getEntity();
            BigDecimal quantityInAdditionalUnit = resource.getDecimalField(ResourceFields.CONVERSION).multiply(maybeQuantity.getRight().get());
            String quantityInAdditionalUnitFormatted = numberService.format(quantityInAdditionalUnit);
            quantityInAdditionalUnitField.setFieldValue(quantityInAdditionalUnitFormatted);
            
        } else {
            quantityInAdditionalUnitField.setFieldValue(null);
        }
    }
    
    public void onQuantityInAdditionalUnitChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent quantityField = (FieldComponent) viewDefinitionState.getComponentByReference(ResourceFields.QUANTITY);
        FieldComponent quantityInAdditionalUnitField = (FieldComponent) viewDefinitionState.getComponentByReference(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT);
        
        Either<Exception, Optional<BigDecimal>> maybeQuantityInAdditionalUnit = BigDecimalUtils.tryParseAndIgnoreSeparator((String) quantityInAdditionalUnitField.getFieldValue(), viewDefinitionState.getLocale());
        if (maybeQuantityInAdditionalUnit.isRight() && maybeQuantityInAdditionalUnit.getRight().isPresent()) {
            FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
            Entity resource = form.getEntity();
            BigDecimal quantity = maybeQuantityInAdditionalUnit.getRight().get().divide(resource.getDecimalField(ResourceFields.CONVERSION), RoundingMode.HALF_UP);
            String quantityFormatted = numberService.format(quantity);
            quantityField.setFieldValue(quantityFormatted);
            
        } else {
            quantityField.setFieldValue(null);
        }
    }
    
    private void fillUnitField(ViewDefinitionState view, Entity resource) {
        FieldComponent givenUnitField = (FieldComponent) view.getComponentByReference(ResourceFields.GIVEN_UNIT);
        givenUnitField.setFieldValue(resource.getStringField(ResourceFields.GIVEN_UNIT));
        givenUnitField.requestComponentUpdateState();
    }
}
