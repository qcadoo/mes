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
package com.qcadoo.mes.materialRequirements.listeners;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.materialRequirements.MaterialRequirementService;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementsConstants;
import com.qcadoo.mes.orders.util.OrderHelperService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MaterialRequirementDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private MaterialRequirementService materialRequirementService;

    @Autowired
    private OrderHelperService orderHelperService;

    public void printMaterialRequirement(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT });
    }

    // TODO KRNA generic candidate
    @Transactional
    public void generateMaterialRequirement(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = view.getComponentByReference("generated");
            ComponentState date = view.getComponentByReference("date");
            ComponentState worker = view.getComponentByReference("worker");

            Entity materialRequirement = dataDefinitionService.get(MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                    MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT).get((Long) state.getFieldValue());

            if (materialRequirement == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.isNotBlank(materialRequirement.getStringField("fileName"))) {
                state.addMessage(
                        "materialRequirements.materialRequirementDetails.window.materialRequirement.documentsWasGenerated",
                        MessageType.FAILURE);
                return;
            }

            List<Entity> orders = materialRequirement.getManyToManyField("orders");
            if (orders.isEmpty()) {
                state.addMessage(
                        "materialRequirements.materialRequirementDetails.window.materialRequirement.missingAssosiatedOrders",
                        MessageType.FAILURE);
                return;
            }
            List<String> numbersOfOrdersWithoutTechnology = orderHelperService.getOrdersWithoutTechnology(orders);
            if (!numbersOfOrdersWithoutTechnology.isEmpty()) {
                state.addMessage(
                        "materialRequirements.materialRequirementDetails.window.materialRequirement.missingTechnologyInOrders",
                        MessageType.FAILURE, StringUtils.join(numbersOfOrdersWithoutTechnology, ",<br>"));
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, LocaleContextHolder.getLocale())
                        .format(new Date()));
            }

            state.performEvent(view, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            materialRequirement = dataDefinitionService.get(MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                    MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT).get((Long) state.getFieldValue());

            try {
                materialRequirementService.generateMaterialRequirementDocuments(state, materialRequirement);
                state.performEvent(view, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

}
