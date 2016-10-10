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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.BasicService;
import com.qcadoo.mes.basic.constants.AddressFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class AddressHooks {

    @Autowired
    private BasicService basicService;

    public boolean validatesWith(final DataDefinition addressDD, final Entity address) {
        boolean isValid = checkAddressType(addressDD, address);

        return isValid;
    }

    private boolean checkAddressType(final DataDefinition addressDD, final Entity address) {
        String addressType = address.getStringField(AddressFields.ADDRESS_TYPE);

        if (basicService.checkIfIsMainAddressType(addressType)) {
            if (!checkIfOnlyOneMainAddressExists(addressDD, address, addressType)) {
                address.addGlobalError("basic.address.error.mainAddressAlreadyExists");

                return false;
            }
        }

        return true;
    }

    private boolean checkIfOnlyOneMainAddressExists(final DataDefinition addressDD, final Entity address, final String addressType) {
        Long addressId = address.getId();

        Entity company = address.getBelongsToField(AddressFields.COMPANY);

        SearchCriteriaBuilder searchCriteriaBuilder = addressDD.find()
                .add(SearchRestrictions.belongsTo(AddressFields.COMPANY, company))
                .add(SearchRestrictions.eq(AddressFields.ADDRESS_TYPE, addressType));

        if (addressId != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", addressId));
        }

        SearchResult searchResult = searchCriteriaBuilder.list();

        return searchResult.getEntities().isEmpty();
    }

    public void onCreate(final DataDefinition addressDD, final Entity address) {
        if (address.getField(AddressFields.CAN_BE_DELETED) == null) {
            address.setField(AddressFields.CAN_BE_DELETED, false);
        }
    }

    public boolean onDelete(final DataDefinition addressDD, final Entity address) {
        String externalNumber = address.getStringField(AddressFields.EXTERNAL_NUMBER);
        String addressType = address.getStringField(AddressFields.ADDRESS_TYPE);

        boolean canBeDeleted = address.getBooleanField(AddressFields.CAN_BE_DELETED);

        if (StringUtils.isEmpty(externalNumber)) {
            if (basicService.checkIfIsMainAddressType(addressType)) {
                if (canBeDeleted) {
                    return true;
                }

                address.addGlobalError("basic.address.error.mainAddressCannotBeDeleted");

                return false;
            }
        } else {
            address.addGlobalError("basic.address.error.addressIsExternalSynchronized");

            return false;
        }

        return true;
    }

}
