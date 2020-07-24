/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orderSupplies.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MaterialRequirementCoverageDetailsListeners {

    @Autowired private OrderSuppliesService orderSuppliesService;

    @Autowired private ReportService reportService;

    @Autowired DataDefinitionService dataDefinitionService;

    public final void printMaterialRequirementCoverage(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(view, "save", args);

            if (!state.isHasError()) {
                FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
                Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

                boolean saved = orderSuppliesService.checkIfMaterialRequirementCoverageIsSaved(materialRequirementCoverageId);

                if (saved) {
                    reportService.printGeneratedReport(view, state,
                            new String[] { args[0], OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                                    OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE });
                } else {
                    view.redirectTo(
                            "/orderSupplies/materialRequirementCoverageReport." + args[0] + "?id=" + state.getFieldValue(), true,
                            false);
                }
            }
        } else {
            state.addMessage("orderSupplies.materialRequirementCoverage.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void deleteCoverage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Set<Long> ids = grid.getSelectedEntitiesIds();
        orderSuppliesService.deleteMaterialRequirementCoverageAndReferences(Lists.newArrayList(ids));

        if (ids.size() == 1) {
            state.addMessage("qcadooView.message.deleteMessage", MessageType.SUCCESS);
        } else {
            state.addMessage("qcadooView.message.deleteMessages", MessageType.SUCCESS,String.valueOf(ids.size()));
        }
    }
}
