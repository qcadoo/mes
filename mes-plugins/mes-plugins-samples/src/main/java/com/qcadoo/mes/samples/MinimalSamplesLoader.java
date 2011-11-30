/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.samples;

import java.util.Map;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.samples.constants.SamplesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityRole;
import com.qcadoo.security.api.SecurityRolesService;

@Component
public class MinimalSamplesLoader extends SamplesLoader {

    @Autowired
    private SecurityRolesService securityRolesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    void loadData(final String dataset, final String locale) {

        if (isEnabled("productionCounting")) {
            setParameters(locale);
        }
        if (isEnabled(SamplesConstants.BASIC_PLUGIN_IDENTIFIER)) {
            readDataFromXML(dataset, "units", locale);
            readDataFromXML(dataset, "shifts", locale);
            readDataFromXML(dataset, "users", locale);
            readDataFromXML(dataset, "company", locale);
        }

    }

    @Override
    void readData(final Map<String, String> values, final String type, final Element node) {

        if ("units".equals(type)) {
            addUnits(values);
        } else if ("shifts".equals(type)) {
            addShifts(values);
        } else if ("users".equals(type)) {
            addUser(values);
        } else if ("company".equals(type)) {
            addCompany(values);
        }
    }

    private void addCompany(final Map<String, String> values) {
        Entity company = dataDefinitionService
                .get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_COMPANY).create();

        company.setField("number", values.get("number"));
        company.setField("companyFullName", values.get("companyfullname"));
        company.setField("tax", values.get("tax"));
        company.setField("street", values.get("street"));
        company.setField("house", values.get("house"));
        company.setField("flat", values.get("flat"));
        company.setField("zipCode", values.get("zipcode"));
        company.setField("city", values.get("city"));
        company.setField("state", values.get("state"));
        company.setField("country", values.get("country"));
        company.setField("email", values.get("email"));
        company.setField("addressWww", values.get("addresswww"));
        company.setField("phone", values.get("phone"));
        company.setField("owner", values.get("owner"));

        company = company.getDataDefinition().save(company);

        validateEntity(company);
    }

    private void setParameters(final String locale) {
        Entity params = dataDefinitionService.get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER,
                SamplesConstants.BASIC_MODEL_PARAMETER).create();

        String alphabeticCode = "";

        if ("pl".equals(locale)) {
            alphabeticCode = "PLN";
        } else {
            alphabeticCode = "USD";
        }

        Entity currency = dataDefinitionService
                .get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_CURRENCY).find()
                .add(SearchRestrictions.eq("alphabeticCode", alphabeticCode)).uniqueResult();

        params.setField("registerQuantityInProduct", true);
        params.setField("registerQuantityOutProduct", true);
        params.setField("registerProductionTime", true);
        params.setField("currency", currency);

        params = params.getDataDefinition().save(params);

        validateEntity(params);
    }

    private void addUser(final Map<String, String> values) {
        Entity user = dataDefinitionService.get("qcadooSecurity", "user").create();

        user.setField("userName", values.get("login"));

        SecurityRole role = securityRolesService.getRoleByIdentifier(values.get("role"));
        user.setField("role", role.getName());
        user.setField("password", values.get("login"));
        user.setField("passwordConfirmation", values.get("login"));
        user.setField("enabled", true);

        user = user.getDataDefinition().save(user);

        validateEntity(user);
    }

    private void addShifts(final Map<String, String> values) {
        Entity shift = dataDefinitionService.get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_SHIFT)
                .create();

        shift.setField("name", values.get("name"));
        shift.setField("mondayWorking", values.get("mondayworking"));
        shift.setField("mondayHours", values.get("mondayhours"));
        shift.setField("tuesdayWorking", values.get("tuesdayworking"));
        shift.setField("tuesdayHours", values.get("tuesdayhours"));
        shift.setField("wensdayWorking", values.get("wensdayworking"));
        shift.setField("wensdayHours", values.get("wensdayhours"));
        shift.setField("thursdayWorking", values.get("thursdayworking"));
        shift.setField("thursdayHours", values.get("thursdayhours"));
        shift.setField("fridayWorking", values.get("fridayworking"));
        shift.setField("fridayHours", values.get("fridayhours"));
        shift.setField("saturdayWorking", values.get("saturdayworking"));
        shift.setField("saturdayHours", values.get("saturdayhours"));
        shift.setField("sundayWorking", values.get("sundayworking"));
        shift.setField("sundayHours", values.get("sundayhours"));

        shift = shift.getDataDefinition().save(shift);

        validateEntity(shift);
    }

    private void addUnits(final Map<String, String> values) {
        Entity dictionary = getDictionaryByName("units");

        Entity unit = dataDefinitionService.get("qcadooModel", "dictionaryItem").create();
        unit.setField("dictionary", dictionary);
        unit.setField("name", values.get("name"));
        unit.setField("description", values.get("description"));

        unit = unit.getDataDefinition().save(unit);

        validateEntity(unit);
    }

    private Entity getDictionaryByName(final String dictionaryName) {
        return dataDefinitionService.get("qcadooModel", "dictionary").find().add(SearchRestrictions.eq("name", dictionaryName))
                .setMaxResults(1).uniqueResult();
    }

}
