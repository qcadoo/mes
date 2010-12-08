/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.menu;

import java.io.IOException;
import java.util.Locale;

import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.components.SimpleValue;
import com.qcadoo.mes.view.containers.FormValue;

@Service
public class MenuService {

    @SuppressWarnings("unchecked")
    public void onDetailsView(final ViewValue<Long> value, final String triggerComponentName, final Entity entity,
            final Locale locale) throws IOException, DocumentException {

        if (value.lookupValue("mainWindow.detailsForm") == null || value.lookupValue("mainWindow.detailsForm").getValue() == null
                || ((FormValue) value.lookupValue("mainWindow.detailsForm").getValue()).getId() == null) {

            ViewValue<SimpleValue> activeValue = (ViewValue<SimpleValue>) value.lookupValue("mainWindow.detailsForm.active");
            if (activeValue != null && activeValue.getValue() != null) {
                activeValue.getValue().setValue("1");
            }
        }
    }
}
