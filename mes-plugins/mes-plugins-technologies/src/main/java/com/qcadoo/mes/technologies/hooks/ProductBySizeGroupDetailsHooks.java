package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductBySizeGroupDetailsHooks {

    @Autowired private UnitService unitService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = formComponent.getEntity();
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(ProductBySizeGroupFields.QUANTITY);
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(ProductBySizeGroupFields.UNIT);
        FieldComponent givenQuantityField = (FieldComponent) view.getComponentByReference(ProductBySizeGroupFields.GIVEN_QUANTITY);
        FieldComponent givenUnitField = (FieldComponent) view.getComponentByReference(ProductBySizeGroupFields.GIVEN_UNIT);

        if(entity.getBelongsToField(ProductBySizeGroupFields.OPERATION_PRODUCT_IN_COMPONENT).getBooleanField(
                OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE)) {
            givenQuantityField.setEnabled(true);
            givenUnitField.setEnabled(true);
        } else {
            givenQuantityField.setEnabled(false);
            givenUnitField.setEnabled(false);

        }
        unitService.fillProductUnitBeforeRenderIfEmpty(view, ProductBySizeGroupFields.UNIT);
        unitService.fillProductUnitBeforeRenderIfEmpty(view, ProductBySizeGroupFields.GIVEN_UNIT);

    }

}
