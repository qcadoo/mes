package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.IssueCommonDetailsHelper;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IssueDetailsHooks {

    @Autowired
    private IssueCommonDetailsHelper issueCommonDetailsHelper;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillUnit(view);
        issueCommonDetailsHelper.setFilterValue(view);
    }

    private void fillUnit(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity issue = form.getPersistedEntityWithIncludedFormValues();
        if(issue.getBooleanField(IssueFields.ISSUED)){
            form.setFormEnabled(false);
            return;
        }
        Entity product = issue.getBelongsToField(IssueFields.PRODUCT);

        FieldComponent issueUnitField = (FieldComponent) view.getComponentByReference("issueQuantityUnit");
        FieldComponent demandUnitField = (FieldComponent) view.getComponentByReference("demandQuantityUnit");
        if (product != null) {
            String unit = product.getStringField(ProductFields.UNIT);
            issueUnitField.setFieldValue(unit);
            demandUnitField.setFieldValue(unit);
        } else {
            issueUnitField.setFieldValue(StringUtils.EMPTY);
            demandUnitField.setFieldValue(StringUtils.EMPTY);
        }
    }
}
