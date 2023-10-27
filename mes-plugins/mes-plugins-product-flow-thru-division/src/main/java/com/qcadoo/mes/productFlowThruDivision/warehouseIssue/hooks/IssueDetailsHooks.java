package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class IssueDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        fillUnit(view);
    }

    private void fillUnit(final ViewDefinitionState view) {
        FormComponent issueForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent issueUnitField = (FieldComponent) view.getComponentByReference("issueQuantityUnit");
        FieldComponent demandUnitField = (FieldComponent) view.getComponentByReference("demandQuantityUnit");

        Entity issue = issueForm.getPersistedEntityWithIncludedFormValues();

        if (issue.getBooleanField(IssueFields.ISSUED)) {
            issueForm.setFormEnabled(false);
            return;
        }

        Entity product = issue.getBelongsToField(IssueFields.PRODUCT);

        if (Objects.nonNull(product)) {
            String unit = product.getStringField(ProductFields.UNIT);

            issueUnitField.setFieldValue(unit);
            demandUnitField.setFieldValue(unit);
        } else {
            issueUnitField.setFieldValue(StringUtils.EMPTY);
            demandUnitField.setFieldValue(StringUtils.EMPTY);
        }
    }

}
