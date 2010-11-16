/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.menu;

import java.io.IOException;
import java.util.Locale;

import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.components.SimpleValue;
import com.qcadoo.mes.view.containers.FormValue;

@Service
public class MenuService {

    @SuppressWarnings("unchecked")
    public void onDetailsView(final ViewValue<Long> value, final String triggerComponentName, final Locale locale)
            throws IOException, DocumentException {

        if (value.lookupValue("mainWindow.detailsForm") == null || value.lookupValue("mainWindow.detailsForm").getValue() == null
                || ((FormValue) value.lookupValue("mainWindow.detailsForm").getValue()).getId() == null) {

            ViewValue<SimpleValue> activeValue = (ViewValue<SimpleValue>) value.lookupValue("mainWindow.detailsForm.active");
            if (activeValue != null && activeValue.getValue() != null) {
                activeValue.getValue().setValue("1");
            }
        }
    }
}
