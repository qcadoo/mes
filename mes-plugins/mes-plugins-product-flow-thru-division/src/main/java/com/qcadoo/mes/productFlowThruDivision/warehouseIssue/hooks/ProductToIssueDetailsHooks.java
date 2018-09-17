package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.IssueCommonDetailsHelper;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductToIssueDetailsHooks {

    @Autowired
    private IssueCommonDetailsHelper issueCommonDetailsHelper;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillUnit(view);
        fillAdditionalUnit(view);
        issueCommonDetailsHelper.setFilterValue(view);
    }

    private void fillUnit(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity productToIssue = form.getPersistedEntityWithIncludedFormValues();
        Entity product = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT);
        FieldComponent demandUnitField = (FieldComponent) view.getComponentByReference("demandQuantityUnit");
        FieldComponent correctionUnitField = (FieldComponent) view.getComponentByReference("correctionUnit");
        if (product != null) {
            String unit = product.getStringField(ProductFields.UNIT);
            demandUnitField.setFieldValue(unit);
            correctionUnitField.setFieldValue(unit);
        } else {
            demandUnitField.setFieldValue(StringUtils.EMPTY);
        }
    }

    private void fillAdditionalUnit(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity productToIssue = form.getPersistedEntityWithIncludedFormValues();
        Entity product = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT);

        if (product != null) {
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            FieldComponent conversionField = (FieldComponent) view.getComponentByReference("conversion");
            if (StringUtils.isEmpty(additionalUnit)) {
                conversionField.setFieldValue(BigDecimal.ONE);
                conversionField.setEnabled(false);
                conversionField.requestComponentUpdateState();

                additionalUnit = product.getStringField(ProductFields.UNIT);
            }
            FieldComponent field = (FieldComponent) view.getComponentByReference("additionalDemandQuantityUnit");
            field.setFieldValue(additionalUnit);
            field.requestComponentUpdateState();
        }

    }

}
