package com.qcadoo.mes.basic;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_COMPANY;
import static com.qcadoo.mes.basic.constants.BasicConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class StaffService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setOwnerCompany(final ViewDefinitionState view) {
        FieldComponent lookup = (FieldComponent) view.getComponentByReference("workFor");
        Entity ownerCompany = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).uniqueResult();
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() != null || lookup.getFieldValue() != null || ownerCompany == null) {
            return;
        }
        lookup.setFieldValue(ownerCompany.getId());
        lookup.requestComponentUpdateState();
    }
}
