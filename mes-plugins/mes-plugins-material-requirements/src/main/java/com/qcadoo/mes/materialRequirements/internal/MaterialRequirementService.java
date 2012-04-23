/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.materialRequirements.internal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialRequirements.internal.constants.MaterialRequirementsConstants;
import com.qcadoo.mes.materialRequirements.internal.print.pdf.MaterialRequirementPdfService;
import com.qcadoo.mes.materialRequirements.internal.print.xls.MaterialRequirementXlsService;
import com.qcadoo.mes.orders.util.OrderHelperService;
import com.qcadoo.mes.orders.util.OrderReportService;
import com.qcadoo.mes.orders.util.RibbonReportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MaterialRequirementService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialRequirementPdfService materialRequirementPdfService;

    @Autowired
    private MaterialRequirementXlsService materialRequirementXlsService;

    @Autowired
    private RibbonReportService ribbonReportService;

    @Autowired
    private OrderReportService orderReportService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderHelperService orderHelperService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("date", null);
        entity.setField("worker", null);
        return true;
    }

    public void disableFormForExistingMaterialRequirement(final ViewDefinitionState state) {
        ComponentState name = state.getComponentByReference("name");
        ComponentState onlyComponents = state.getComponentByReference("onlyComponents");
        ComponentState materialRequirementComponents = state.getComponentByReference("materialRequirementComponents");
        FieldComponent generated = (FieldComponent) state.getComponentByReference("generated");

        if ("1".equals(generated.getFieldValue())) {
            name.setEnabled(false);
            onlyComponents.setEnabled(false);
            materialRequirementComponents.setEnabled(false);
        } else {
            name.setEnabled(true);
            onlyComponents.setEnabled(true);
        }
    }

    public void setGenerateButtonState(final ViewDefinitionState state) {
        ribbonReportService.setGenerateButtonState(state, state.getLocale(), MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT);
    }

    public void setGridGenerateButtonState(final ViewDefinitionState state) {
        ribbonReportService.setGridGenerateButtonState(state, state.getLocale(), MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT);
    }

    // TODO KRNA generic candidate
    @Transactional
    public void generateMaterialRequirement(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

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

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            materialRequirement = dataDefinitionService.get(MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                    MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT).get((Long) state.getFieldValue());

            try {
                generateMaterialReqDocuments(state, materialRequirement);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public void printMaterialRequirement(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                MaterialRequirementsConstants.PLUGIN_IDENTIFIER, MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT });
    }

    public void printMaterialReqForOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Entity materialRequirement = printMaterialReqForOrder(state);
        if (materialRequirement == null) {
            return;
        }
        try {
            generateMaterialReqDocuments(state, materialRequirement);
            viewDefinitionState.redirectTo(
                    "/generateSavedReport/" + MaterialRequirementsConstants.PLUGIN_IDENTIFIER + "/"
                            + MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT + "." + args[0] + "?id="
                            + materialRequirement.getId(), true, false);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void generateMaterialReqDocuments(final ComponentState state, final Entity materialRequirement) throws IOException,
            DocumentException {
        Entity materialRequirementWithFileName = fileService.updateReportFileName(materialRequirement, "date",
                "materialRequirements.materialRequirement.report.fileName");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        materialRequirementPdfService.generateDocument(materialRequirementWithFileName, company, state.getLocale());
        materialRequirementXlsService.generateDocument(materialRequirementWithFileName, company, state.getLocale());
    }

    private Entity printMaterialReqForOrder(final ComponentState state) {

        Map<String, Object> entityFieldsMap = new HashMap<String, Object>();
        entityFieldsMap.put("onlyComponents", false);

        OrderReportService.OrderValidator orderValidator = new OrderReportService.OrderValidator() {

            @Override
            public ErrorMessage validateOrder(final Entity order) {
                if (order.getField("technology") == null) {
                    return new ErrorMessage("orders.validate.global.error.orderMustHaveTechnology",
                            order.getStringField("number"));
                }
                return null;
            }
        };

        return orderReportService.printForOrder(state, MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT, entityFieldsMap, orderValidator);
    }

}
