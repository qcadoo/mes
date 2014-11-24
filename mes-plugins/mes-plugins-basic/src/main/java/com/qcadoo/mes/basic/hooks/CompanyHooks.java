/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.CountryFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyHooks {

    private static final String L_PL = "PL";

    @Autowired
    private ParameterService parameterService;

    public boolean validatesWith(final DataDefinition companyDD, final Entity company) {
        boolean isValid = checkIfTaxIsValid(companyDD, company);

        return isValid;
    }

    public boolean checkIfTaxIsValid(final DataDefinition companyDD, final Entity company) {
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
    }

    public void setDefaultCountry(final DataDefinition companyDD, final Entity company) {
        Entity country = company.getBelongsToField(COUNTRY);

        if (country == null) {
            company.setField(COUNTRY, getDefaultCountry());
        }
    }

    private Entity getDefaultCountry() {
        return parameterService.getParameter().getBelongsToField(COUNTRY);
    }

    public void onCopy(final DataDefinition companyDD, final Entity company) {
        clearSpecyfiedFields(company);
    }

    private void clearSpecyfiedFields(final Entity company) {
        company.setField(CompanyFields.EXTERNAL_NUMBER, null);
    }

}
