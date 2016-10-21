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
package com.qcadoo.mes.productionPerShift.hooks;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionPerShift.constants.PPSReportFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public final class PPSReportDetailsHooks {

    private static final String L_FORM = "form";

    private static final List<String> REPORT_FIELDS = Arrays.asList(PPSReportFields.NUMBER, PPSReportFields.NAME,
            PPSReportFields.DATE_FROM, PPSReportFields.DATE_TO);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateReportNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PPS_REPORT, L_FORM, PPSReportFields.NUMBER);
    }

    public void disableFields(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Long reportId = form.getEntityId();

        if (reportId == null) {
            setFieldsState(view, REPORT_FIELDS, true);
        } else {
            Entity report = getReportFromDb(reportId);

            if (report != null) {
                boolean generated = report.getBooleanField(PPSReportFields.GENERATED);

                if (generated) {
                    setFieldsState(view, REPORT_FIELDS, false);
                } else {
                    setFieldsState(view, REPORT_FIELDS, true);
                }
            }
        }
    }

    private void setFieldsState(final ViewDefinitionState view, final List<String> fieldNames, final boolean enabled) {
        for (String fieldName : fieldNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldName);

            fieldComponent.setEnabled(enabled);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private Entity getReportFromDb(final Long goodFoodReportId) {
        return dataDefinitionService
                .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_PPS_REPORT)
                .get(goodFoodReportId);
    }

}
