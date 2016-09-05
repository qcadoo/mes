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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.AddressFields;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.model.constants.QcadooModelConstants;

@Service
public class BasicService {

    private static final String L_ADDRESS_TYPE = "addressType";

    private static final String L_MAIN = "01main";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public String getMainAddressType() {
        String addressType = null;

        Optional<Entity> mayBeMainAddressType = getMainAddressTypeDictionaryItem();

        if (mayBeMainAddressType.isPresent()) {
            Entity mainAddressType = mayBeMainAddressType.get();

            addressType = mainAddressType.getStringField(DictionaryItemFields.NAME);
        }

        return addressType;
    }

    private Optional<Entity> getMainAddressTypeDictionaryItem() {
        Optional<Entity> mayBeAddressType = getAddressTypeDictionary();

        if (mayBeAddressType.isPresent()) {
            Entity addressType = mayBeAddressType.get();

            return Optional.ofNullable(findMainAddressTypeDictionaryItem(addressType));
        }

        return Optional.empty();
    }

    private Entity findMainAddressTypeDictionaryItem(final Entity addressType) {
        return getDictionaryItemDD().find().add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, addressType))
                .add(SearchRestrictions.eq(DictionaryItemFields.TECHNICAL_CODE, L_MAIN)).setMaxResults(1).uniqueResult();
    }

    private Optional<Entity> getAddressTypeDictionary() {
        return Optional.ofNullable(findAddressTypeDictionary());
    }

    private Entity findAddressTypeDictionary() {
        return getDictionaryDD().find().add(SearchRestrictions.eq(DictionaryFields.NAME, L_ADDRESS_TYPE)).setMaxResults(1)
                .uniqueResult();
    }

    public Optional<Entity> getMainAddress(final Entity company) {
        String addressType = getMainAddressType();

        if (StringUtils.isNotEmpty(addressType)) {
            return Optional.ofNullable(findMainAddress(company, addressType));
        }

        return Optional.empty();
    }

    private Entity findMainAddress(final Entity company, final String addressType) {
        return getAddressDD().find().add(SearchRestrictions.belongsTo(AddressFields.COMPANY, company))
                .add(SearchRestrictions.eq(AddressFields.ADDRESS_TYPE, addressType)).setMaxResults(1).uniqueResult();
    }

    public String getAddressesNumber(final Entity company) {
        String number;

        if (company.getId() == null) {
            number = "01";
        } else {
            Integer addressesCount = getAddressesCount(company);

            addressesCount = addressesCount + 1;

            number = String.format("%02d", addressesCount);
        }

        return number;
    }

    public boolean checkIfIsMainAddressType(final String addressType) {
        if (StringUtils.isNotEmpty(addressType)) {
            String mainAddressType = getMainAddressType();

            if (StringUtils.isNotEmpty(mainAddressType)) {
                if (mainAddressType.equals(addressType)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Integer getAddressesCount(final Entity company) {
        return getAddressDD().find().add(SearchRestrictions.belongsTo(AddressFields.COMPANY, company)).list()
                .getTotalNumberOfEntities();
    }

    public DataDefinition getDictionaryDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY);
    }

    public DataDefinition getDictionaryItemDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM);
    }

    public DataDefinition getAddressDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_ADDRESS);
    }

}
