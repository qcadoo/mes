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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.PalletNumberGenerator;
import com.qcadoo.mes.basic.PalletNumbersService;
import com.qcadoo.mes.basic.constants.PalletNumberHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PalletNumberHelperHooks {

    @Autowired
    private PalletNumbersService palletNumbersService;

    @Autowired
    private PalletNumberGenerator palletNumberGenerator;

    public void onCreate(final DataDefinition palletNumberHelperDD, final Entity palletNumberHelper) {
        generatePalletNumbers(palletNumberHelper);
    }

    public void onCopy(final DataDefinition palletNumberHelperDD, final Entity palletNumberHelper) {
        generatePalletNumbers(palletNumberHelper);
    }

    private void generatePalletNumbers(final Entity palletNumberHelper) {
        Integer quantity = getQuantity(palletNumberHelper);

        List<Entity> palletNumbers = palletNumberHelper.getManyToManyField(PalletNumberHelperFields.PALLET_NUMBERS);

        if ((quantity != null) && palletNumbers.isEmpty()) {
            String firstNumber = palletNumberGenerator.generate();

            palletNumbers = palletNumbersService.createPalletNumbers(palletNumberGenerator.list(firstNumber, quantity));

            palletNumberHelper.setField(PalletNumberHelperFields.TEMPORARY, false);

            palletNumberHelper.setField(PalletNumberHelperFields.PALLET_NUMBERS, palletNumbers);
        }
    }

    private Integer getQuantity(final Entity palletNumberHelper) {
        Integer quantity = null;

        Object fieldValue = palletNumberHelper.getField(PalletNumberHelperFields.QUANTITY);

        if (fieldValue != null) {
            if (fieldValue instanceof Long) {
                quantity = ((Long) fieldValue).intValue();
            }
            if (fieldValue instanceof Integer) {
                quantity = (Integer) fieldValue;
            }
        }

        return quantity;
    }

}
