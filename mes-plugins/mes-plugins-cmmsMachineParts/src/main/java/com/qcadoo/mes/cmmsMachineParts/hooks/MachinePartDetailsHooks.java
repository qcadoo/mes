package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class MachinePartDetailsHooks {

    public void setMachinePartCheckbox(final ViewDefinitionState view) {
        CheckBoxComponent machinePartCheckbox = (CheckBoxComponent) view.getComponentByReference("machinePart");
        machinePartCheckbox.setChecked(true);
        machinePartCheckbox.requestComponentUpdateState();
    }

    public void toggleSuppliersGrids(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity machinePart = form.getPersistedEntityWithIncludedFormValues();
        GridComponent productCompanies = (GridComponent) view.getComponentByReference("productCompanies");
        GridComponent productsFamilyCompanies = (GridComponent) view.getComponentByReference("productsFamilyCompanies");
        if (ProductFamilyElementType.from(machinePart).compareTo(ProductFamilyElementType.PARTICULAR_PRODUCT) == 0) {
            productCompanies.setVisible(true);
            productsFamilyCompanies.setVisible(false);
        } else {
            productCompanies.setVisible(false);
            productsFamilyCompanies.setVisible(true);
        }
    }
}
