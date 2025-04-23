package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.RepackingFields;
import com.qcadoo.mes.materialFlowResources.constants.RepackingPositionFields;
import com.qcadoo.mes.materialFlowResources.criteriaModifiers.ResourceCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RepackingPositionDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity repackingPosition = form.getPersistedEntityWithIncludedFormValues();
        Entity product = repackingPosition.getBelongsToField(RepackingPositionFields.PRODUCT);
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(RepackingPositionFields.UNIT);
        FieldComponent additionalUnitField = (FieldComponent) view.getComponentByReference(RepackingPositionFields.ADDITIONAL_UNIT);
        if (product != null) {
            unitField.setFieldValue(product.getStringField(ProductFields.UNIT));
            additionalUnitField.setFieldValue(Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(product.getStringField(ProductFields.UNIT)));
        }
        LookupComponent resourceLookup = (LookupComponent) view.getComponentByReference(RepackingPositionFields.RESOURCE);
        resourceLookup.setRequired(true);
        FilterValueHolder filter = resourceLookup.getFilterValue();
        filter.put(ResourceCriteriaModifiers.L_LOCATION_FROM, repackingPosition.getBelongsToField(RepackingPositionFields.REPACKING)
                .getBelongsToField(RepackingFields.LOCATION).getId());
        resourceLookup.setFilterValue(filter);
    }
}
