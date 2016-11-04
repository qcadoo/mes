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
package com.qcadoo.mes.productionPerShift.hooks;

import com.google.common.base.Strings;
import com.qcadoo.mes.productionPerShift.constants.ParameterFieldsPPS;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;


@Service
public class ParameterHooksPPS {


    public void validatesWith(final DataDefinition parameterDD, final Entity parameter) {
        boolean ppsIsAutomatic = parameter.getBooleanField(ParameterFieldsPPS.PPS_IS_AUTOMATIC);
        String ppsAlgorithm = parameter.getStringField(ParameterFieldsPPS.PPS_ALGORITHM);
        if(ppsIsAutomatic && Strings.isNullOrEmpty(ppsAlgorithm)){
            parameter.addError(parameterDD.getField(ParameterFieldsPPS.PPS_ALGORITHM), "basic.parameter.ppsAlgorithm.isRequired");
        }
    }

}
