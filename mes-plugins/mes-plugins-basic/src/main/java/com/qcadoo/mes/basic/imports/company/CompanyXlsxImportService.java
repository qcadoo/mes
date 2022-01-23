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
package com.qcadoo.mes.basic.imports.company;

import static com.qcadoo.mes.basic.constants.CompanyFields.COUNTRY;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyXlsxImportService extends XlsxImportService {

    @Autowired
    private ParameterService parameterService;

    @Override
    public void validateEntity(final Entity company, final DataDefinition companyDD) {
        validateCountry(company, companyDD);
    }

    private void validateCountry(final Entity company, final DataDefinition companyDD) {
        Entity country = company.getBelongsToField(CompanyFields.COUNTRY);

        if (!Objects.nonNull(country)) {
            country = getDefaultCountry();
        }

        company.setField(CompanyFields.COUNTRY, country);
        company.setField(CompanyFields.TAX_COUNTRY_CODE, country);
    }

    private Entity getDefaultCountry() {
        return parameterService.getParameter().getBelongsToField(COUNTRY);
    }

}
