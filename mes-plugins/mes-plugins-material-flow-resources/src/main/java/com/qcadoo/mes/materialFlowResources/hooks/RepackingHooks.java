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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.RepackingFields;
import com.qcadoo.mes.materialFlowResources.states.constants.RepackingState;
import com.qcadoo.mes.materialFlowResources.states.constants.RepackingStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

@Service
public class RepackingHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private RepackingStateChangeDescriber describer;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onCreate(final DataDefinition repackingDD, final Entity repacking) {
        setInitialState(repacking);
    }

    private void setInitialState(final Entity repacking) {
        stateChangeEntityBuilder.buildInitial(describer, repacking, RepackingState.DRAFT);
    }

    public void onSave(final DataDefinition repackingDD, final Entity repacking) {
        setNumber(repacking);
    }

    private void setNumber(final Entity repacking) {
        if (checkIfShouldInsertNumber(repacking)) {
            String number = jdbcTemplate.queryForObject("select generate_repacking_number()", Collections.emptyMap(),
                    String.class);
            repacking.setField(RepackingFields.NUMBER, number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity repacking) {
        if (!Objects.isNull(repacking.getId())) {
            return false;
        }
        return !StringUtils.isNotBlank(repacking.getStringField(RepackingFields.NUMBER));
    }

}
