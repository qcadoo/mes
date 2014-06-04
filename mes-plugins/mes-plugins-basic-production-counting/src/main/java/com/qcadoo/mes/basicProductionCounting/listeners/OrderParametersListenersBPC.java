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
package com.qcadoo.mes.basicProductionCounting.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ParameterFieldsBPC;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;

@Service
public class OrderParametersListenersBPC {

    private static final Logger LOG = LoggerFactory.getLogger(OrderParametersListenersBPC.class);

    public void onChangeLockProductionProgress(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        CheckBoxComponent lockProductionProgressCheckBox = (CheckBoxComponent) componentState;
        CheckBoxComponent lockOrderPlannedQuantityCheckBox = (CheckBoxComponent) viewState
                .getComponentByReference(ParameterFieldsO.BLOCK_ABILILITY_TO_CHANGE_APPROVAL_ORDER);

        if (lockOrderPlannedQuantityCheckBox == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("orderParameters view: can't find component with reference='%s'",
                        ParameterFieldsO.BLOCK_ABILILITY_TO_CHANGE_APPROVAL_ORDER));
            }
            return;
        }
        if (lockOrderPlannedQuantityCheckBox.isEnabled() && lockProductionProgressCheckBox.isChecked()) {
            lockOrderPlannedQuantityCheckBox.setChecked(true);
        }
    }

    public void onChangeLockOrderPlannedQuantity(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        CheckBoxComponent lockOrderPlannedQuantityCheckBox = (CheckBoxComponent) componentState;
        CheckBoxComponent lockProgressCheckBox = (CheckBoxComponent) viewState
                .getComponentByReference(ParameterFieldsBPC.LOCK_PRODUCTION_PROGRESS);
        if (!lockOrderPlannedQuantityCheckBox.isChecked()) {
            lockProgressCheckBox.setChecked(false);
        }
    }

}
