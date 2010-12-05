/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.menu;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.view.ViewDefinitionState;

@Service
public class MenuService {

    public void selectCheckboxIfActiveIsUndefined(final ViewDefinitionState state, final Locale locale) {
        // if (value.lookupValue("mainWindow.detailsForm") == null || value.lookupValue("mainWindow.detailsForm").getValue() ==
        // null
        // || ((FormValue) value.lookupValue("mainWindow.detailsForm").getValue()).getId() == null) {
        // ViewValue<SimpleValue> activeValue = (ViewValue<SimpleValue>) value.lookupValue("mainWindow.detailsForm.active");
        // if (activeValue != null && activeValue.getValue() != null) {
        // activeValue.getValue().setValue("1");
        // }
        // }
    }

}
