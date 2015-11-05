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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.PalletNumberHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;

@Service
@RunIfEnabled(BasicConstants.PLUGIN_IDENTIFIER)
public class PalletNumbersServiceImpl implements PalletNumbersService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MultiTenantService multiTenantService;

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
    public Entity createPalletNumberHelper(final Integer quantity, final boolean temporary, final List<Entity> palletNumbers) {
        Entity palletNumberHelper = getPalletNumberHelperDD().create();

        palletNumberHelper.setField(PalletNumberHelperFields.QUANTITY, quantity);
        palletNumberHelper.setField(PalletNumberHelperFields.TEMPORARY, temporary);

        palletNumberHelper.setField(PalletNumberHelperFields.PALLET_NUMBERS, palletNumbers);

        palletNumberHelper = palletNumberHelper.getDataDefinition().save(palletNumberHelper);

        return palletNumberHelper;
    }

    @Override
    public Entity getPalletNumber(final Long palletNumberId) {
        return getPalletNumberDD().get(palletNumberId);
    }

    @Override
    public List<Entity> getPalletNumbers(final Set<Long> palletNumberIds) {
        return palletNumberIds.stream().map(palletNumberId -> getPalletNumber(palletNumberId)).collect(Collectors.toList());
    }

    @Override
    public List<String> getNumbers(final List<Entity> palletNumbers) {
        List<String> numbers = palletNumbers.stream().map(palletNumber -> palletNumber.getStringField(PalletNumberFields.NUMBER))
                .collect(Collectors.toList());

        numbers.sort(Comparator.<String>naturalOrder());

        return numbers;
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

    @Override
    public void deleteTemporaryPalletNumberHelpersTrigger() {
        multiTenantService.doInMultiTenantContext(new MultiTenantCallback() {

            @Override
            public void invoke() {
                deleteTemporaryPalletNumberHelpers();
            }

        });
    }

    private void deleteTemporaryPalletNumberHelpers() {
        List<Entity> palletNumberHelpers = getPalletNumberHelperDD().find()
                .add(SearchRestrictions.eq(PalletNumberHelperFields.TEMPORARY, true)).list().getEntities();

        palletNumberHelpers.forEach(palletNumberHelper -> palletNumberHelper.getDataDefinition().delete(
                palletNumberHelper.getId()));
    }

}
