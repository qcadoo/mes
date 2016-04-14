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
package com.qcadoo.mes.basic;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import java.util.List;

@Service
public class PalletNumberGenerator {

    private static final String L_FORM = "form";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    /**
     * Check if should insert number
     *
     * @param view
     *            View
     *
     * @return true/false
     *
     */
    public boolean checkIfShouldInsertNumber(final ViewDefinitionState view) {
        return numberGeneratorService.checkIfShouldInsertNumber(view, L_FORM, PalletNumberFields.NUMBER);
    }

    /**
     * Generate number
     *
     * @return number
     */
    public String generate() {
        return numberGeneratorService.generateNumber(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PALLET_NUMBER, 6);
    }

    /**
     * Lists pallet numbers
     *
     * @param firstNumber
     *            First number
     *
     * @param quantity
     *            Quantity
     *
     * @return palletNumbers
     *
     */
    public List<String> list(final String firstNumber, final int quantity) {
        LinkedList<String> palletNumbers = Lists.newLinkedList();

        Integer highestNumber = Integer.valueOf(firstNumber);

        for (int i = 0; i < quantity; i++) {
            Integer nextNumber = highestNumber + i;

            String palletNumber = String.format("%06d", nextNumber);

            palletNumbers.add(palletNumber);
        }

        return palletNumbers;
    }

}
