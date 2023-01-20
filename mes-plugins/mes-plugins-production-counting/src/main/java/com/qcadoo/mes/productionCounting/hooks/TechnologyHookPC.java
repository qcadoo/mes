/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.hooks;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyHookPC {

    private static final List<String> L_TECHNOLOGY_FIELD_NAMES = Lists.newArrayList(
            TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
            TechnologyFieldsPC.REGISTER_QUANTITY_IN_PRODUCT, TechnologyFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT,
            TechnologyFieldsPC.REGISTER_PRODUCTION_TIME);

    @Autowired
    private ParameterService parameterService;

    public void onCreate(final DataDefinition technologyDD, final Entity technology) {
        setTechnologyWithDefaultProductionCountingValues(technologyDD, technology);
    }

    public void setTechnologyWithDefaultProductionCountingValues(final DataDefinition technologyDD, final Entity technology) {
        for (String fieldName : L_TECHNOLOGY_FIELD_NAMES) {
            if (Objects.isNull(technology.getField(fieldName))) {
                technology.setField(fieldName, parameterService.getParameter().getField(fieldName));
            }
        }
    }

    public boolean validatesWith(final DataDefinition technologyDD, final Entity technology) {
        if (Objects.isNull(technology.getStringField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            technology.addError(technologyDD.getField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING),
                    "qcadooView.validate.field.error.missing");

            return false;
        }

        if (technology.getBooleanField(TechnologyFieldsPC.PIECEWORK_PRODUCTION) && Objects.isNull(technology.getBelongsToField(TechnologyFieldsPC.PIECE_RATE))) {
            technology.addError(technologyDD.getField(TechnologyFieldsPC.PIECE_RATE),
                    "qcadooView.validate.field.error.missing");

            return false;
        }

        return true;
    }

}
