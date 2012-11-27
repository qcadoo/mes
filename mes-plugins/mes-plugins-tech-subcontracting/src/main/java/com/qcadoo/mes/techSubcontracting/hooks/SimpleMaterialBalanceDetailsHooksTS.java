package com.qcadoo.mes.techSubcontracting.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class SimpleMaterialBalanceDetailsHooksTS {

    public void setMrpAlgoritmValue(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() != null) {
            return;
        }
        FieldComponent mrpAlgoritm = (FieldComponent) view.getComponentByReference("mrpAlgorithm");
        mrpAlgoritm.setFieldValue("03componentsAndSubcontractorsProducts");
        mrpAlgoritm.requestComponentUpdateState();
    }
}
