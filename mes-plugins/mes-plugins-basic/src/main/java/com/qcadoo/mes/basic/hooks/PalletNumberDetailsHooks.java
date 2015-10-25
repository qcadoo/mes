/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.basic.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.PalletNumberGenerator;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class PalletNumberDetailsHooks {

    @Autowired
    private PalletNumberGenerator palletNumberGenerator;

    public void onBeforeRender(final ViewDefinitionState view) {
        generatePalletNumber(view);
    }

    private void generatePalletNumber(final ViewDefinitionState view) {
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(PalletNumberFields.NUMBER);

        if (palletNumberGenerator.checkIfShouldInsertNumber(view)) {
            String number = palletNumberGenerator.generate();

            numberField.setFieldValue(number);
            numberField.requestComponentUpdateState();
        }
    }

}
