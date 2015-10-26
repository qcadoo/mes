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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class PalletNumbersServiceImpl implements PalletNumbersService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity createPalletNumber(final String number) {
        Entity palletNumber = getPalletNumberDD().create();

        palletNumber.setField(PalletNumberFields.NUMBER, number);

        palletNumber = palletNumber.getDataDefinition().save(palletNumber);

        return palletNumber;
    }

    @Override
    public List<Entity> createPalletNumbers(final List<String> numbers) {
        List<Entity> palletNumbers = Lists.newArrayList();

        for (String number : numbers) {
            Entity palletNumber = createPalletNumber(number);

            palletNumbers.add(palletNumber);
        }

        return palletNumbers;
    }

    @Override
    public Entity getPalletNumber(final Long palletNumberId) {
        return getPalletNumberDD().get(palletNumberId);
    }

    @Override
    public DataDefinition getPalletNumberDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PALLET_NUMBER);
    }

    @Override
    public Entity getPalletNumberHelper(final Long palletNumberHelperId) {
        return getPalletNumberHelperDD().get(palletNumberHelperId);
    }

    @Override
    public DataDefinition getPalletNumberHelperDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PALLET_NUMBER_HELPER);
    }

}
