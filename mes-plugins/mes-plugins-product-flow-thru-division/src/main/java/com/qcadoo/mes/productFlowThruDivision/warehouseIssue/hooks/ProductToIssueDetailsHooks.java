package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class ProductToIssueDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        fillUnit(view);
        fillAdditionalUnit(view);
    }

    private void fillUnit(final ViewDefinitionState view) {
        FormComponent productToIssueForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent demandQuantityUnitField = (FieldComponent) view.getComponentByReference("demandQuantityUnit");
        FieldComponent correctionUnitField = (FieldComponent) view.getComponentByReference("correctionUnit");

        Entity productToIssue = productToIssueForm.getPersistedEntityWithIncludedFormValues();

        Entity product = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT);

        if (Objects.nonNull(product)) {
            String unit = product.getStringField(ProductFields.UNIT);

            demandQuantityUnitField.setFieldValue(unit);
            correctionUnitField.setFieldValue(unit);
        } else {
            demandQuantityUnitField.setFieldValue(StringUtils.EMPTY);
        }
    }

    private void fillAdditionalUnit(final ViewDefinitionState view) {
        FormComponent productToIssueForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent conversionField = (FieldComponent) view.getComponentByReference("conversion");
        FieldComponent additionalDemandQuantityUnitField = (FieldComponent) view.getComponentByReference("additionalDemandQuantityUnit");

        Entity productToIssue = productToIssueForm.getPersistedEntityWithIncludedFormValues();

        Entity product = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT);

        if (Objects.nonNull(product)) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

            if (StringUtils.isEmpty(additionalUnit)) {
                conversionField.setFieldValue(BigDecimal.ONE);
                conversionField.setEnabled(false);
                conversionField.requestComponentUpdateState();

                additionalUnit = product.getStringField(ProductFields.UNIT);
            }

            additionalDemandQuantityUnitField.setFieldValue(additionalUnit);
            additionalDemandQuantityUnitField.requestComponentUpdateState();
        }
    }

}
