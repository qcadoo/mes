package com.qcadoo.mes.techSubcontracting.hooks;

import static com.qcadoo.mes.technologies.constants.MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS;

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
        mrpAlgoritm.setFieldValue(COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS.getStringValue());
        mrpAlgoritm.requestComponentUpdateState();
    }

}
