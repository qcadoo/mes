package com.qcadoo.mes.basic.hooks;

import static com.qcadoo.mes.basic.constants.CompanyFields.COUNTRY;
import static com.qcadoo.mes.basic.constants.CompanyFields.TAX;
import static com.qcadoo.mes.basic.constants.CompanyFields.TAX_COUNTRY_CODE;
import static com.qcadoo.mes.basic.constants.CountryFields.CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
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
        String tax = company.getStringField(TAX);

        if (!checkIfTaxForPLIsValid(taxCountryCode, tax)) {
            company.addError(companyDD.getField(TAX), "basic.company.tax.error.taxIsNotValid");
            return false;
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
                sum += Integer.parseInt(taxNumbers[i + 1]) * weights[i];
            }

            return (sum % 11) == Integer.parseInt(taxNumbers[10]);
        } catch (NumberFormatException e) {
            return false;
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
