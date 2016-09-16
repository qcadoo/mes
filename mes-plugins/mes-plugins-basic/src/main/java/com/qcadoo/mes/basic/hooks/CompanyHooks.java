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

import static com.qcadoo.mes.basic.constants.CompanyFields.COUNTRY;
import static com.qcadoo.mes.basic.constants.CompanyFields.TAX;
import static com.qcadoo.mes.basic.constants.CompanyFields.TAX_COUNTRY_CODE;
import static com.qcadoo.mes.basic.constants.CountryFields.CODE;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.BasicService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.AddressFields;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.CountryFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyHooks {

    private static final String L_PL = "PL";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private BasicService basicService;

    public boolean validatesWith(final DataDefinition companyDD, final Entity company) {
        boolean isValid = checkIfTaxIsValid(companyDD, company);

        return isValid;
    }

    private boolean checkIfTaxIsValid(final DataDefinition companyDD, final Entity company) {
        Entity taxCountryCode = company.getBelongsToField(TAX_COUNTRY_CODE);
        if (taxCountryCode != null && taxCountryCode.getStringField(CountryFields.CODE).equals(L_PL)) {
            String tax = company.getStringField(TAX);

            if (!checkIfTaxForPLIsValid(taxCountryCode, tax)) {
                company.addError(companyDD.getField(TAX), "basic.company.tax.error.taxIsNotValid");
                return false;
            }
        }

        return true;
    }

    private boolean checkIfTaxForPLIsValid(final Entity taxCountryCode, final String tax) {
        if (checkIfTaxCountryCodeIsPL(taxCountryCode)) {
            return checkIfTaxIsValid(tax);
        } else {
            return true;
        }
    }

    private boolean checkIfTaxCountryCodeIsPL(final Entity taxCountryCode) {
        if (taxCountryCode == null) {
            Entity defaultCountry = getDefaultCountry();

            if (defaultCountry == null) {
                return false;
            } else {
                return (L_PL.equals(defaultCountry.getStringField(CODE)));
            }
        } else {
            return (L_PL.equals(taxCountryCode.getStringField(CODE)));
        }
    }

    private boolean checkIfTaxIsValid(String tax) {
        if (tax == null) {
            return true;
        } else {
            if (tax.length() == 13) {
                tax = tax.replaceAll("-", "");
            }

            if (tax.length() != 10) {
                return false;
            }

            int[] weights = { 6, 5, 7, 2, 3, 4, 5, 6, 7 };

            String[] taxNumbers = tax.split("");

            try {
                int sum = 0;

                for (int i = 0; i < weights.length; i++) {
                    sum += Integer.parseInt(taxNumbers[i]) * weights[i];
                }

                return (sum % 11) == Integer.parseInt(taxNumbers[9]);
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public void onCreate(final DataDefinition companyDD, final Entity company) {
        setDefaultCountry(companyDD, company);
        setDefaultParameters(companyDD, company);
        setMainAddress(company);
    }

    private void setDefaultCountry(final DataDefinition companyDD, final Entity company) {
        Entity country = company.getBelongsToField(COUNTRY);

        if (country == null) {
            company.setField(COUNTRY, getDefaultCountry());
        }
    }

    private void setDefaultParameters(final DataDefinition companyDD, final Entity company) {
        if (company.getField(CompanyFields.IS_SUPPLIER) == null) {
            company.setField(CompanyFields.IS_SUPPLIER, false);
        }
        if (company.getField(CompanyFields.IS_RECEIVER) == null) {
            company.setField(CompanyFields.IS_RECEIVER, false);
        }
    }

    private void setMainAddress(final Entity company) {
        company.setField(CompanyFields.ADDRESSES, Lists.newArrayList(createMainAddress(company)));
    }

    private Entity createMainAddress(final Entity company) {
        Entity address = basicService.getAddressDD().create();

        updateMainAddress(address, company);

        return address;
    }

    private void updateMainAddress(final Entity address, final Entity company) {
        address.setField(AddressFields.ADDRESS_TYPE, basicService.getMainAddressType());

        if (address.getId() == null) {
            address.setField(AddressFields.NUMBER, basicService.generateAddressNumber(company));
        }

        address.setField(AddressFields.PHONE, company.getStringField(CompanyFields.PHONE));
        address.setField(AddressFields.EMAIL, company.getStringField(CompanyFields.EMAIL));
        address.setField(AddressFields.WEBSITE, company.getStringField(CompanyFields.WEBSITE));
        address.setField(AddressFields.STREET, company.getStringField(CompanyFields.STREET));
        address.setField(AddressFields.HOUSE, company.getStringField(CompanyFields.HOUSE));
        address.setField(AddressFields.FLAT, company.getStringField(CompanyFields.FLAT));
        address.setField(AddressFields.ZIP_CODE, company.getStringField(CompanyFields.ZIP_CODE));
        address.setField(AddressFields.CITY, company.getStringField(CompanyFields.CITY));
        address.setField(AddressFields.STATE, company.getStringField(CompanyFields.STATE));
        address.setField(AddressFields.COUNTRY, company.getBelongsToField(CompanyFields.COUNTRY));
        address.setField(AddressFields.CONTACT_PERSON, company.getStringField(CompanyFields.CONTACT_PERSON));
        address.setField(AddressFields.CAN_BE_DELETED, false);
    }

    private Entity getDefaultCountry() {
        return parameterService.getParameter().getBelongsToField(COUNTRY);
    }

    public void onSave(final DataDefinition companyDD, final Entity company) {
        updateMainAddress(company);
    }

    private void updateMainAddress(final Entity company) {
        Optional<Entity> mayBeAddress = basicService.getMainAddress(company);

        if (mayBeAddress.isPresent()) {
            Entity address = mayBeAddress.get();

            updateMainAddress(address, company);

            address = address.getDataDefinition().save(address);
        } else {
            setMainAddress(company);
        }
    }

    public void onCopy(final DataDefinition companyDD, final Entity company) {
        setMainAddress(company);
        clearSpecyfiedFields(company);
    }

    private void clearSpecyfiedFields(final Entity company) {
        company.setField(CompanyFields.EXTERNAL_NUMBER, null);
    }

    public void onDelete(final DataDefinition companyDD, final Entity company) {
        setAddressesCanBeDeleted(company);
    }

    private void setAddressesCanBeDeleted(final Entity company) {
        List<Entity> addresses = company.getHasManyField(CompanyFields.ADDRESSES);

        addresses.forEach(address -> {
            address.setField(AddressFields.CAN_BE_DELETED, true);

            address = address.getDataDefinition().save(address);
        });
    }

}
