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
package com.qcadoo.mes.technologies.listeners;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.imports.operationProductInComponent.OperationProductInComponentCellBinderRegistry;
import com.qcadoo.mes.technologies.imports.operationProductInComponent.OperationProductInComponentXlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OperationProductInComponentsImportListeners {



    @Autowired
    private OperationProductInComponentXlsxImportService operationProductInComponentXlsxImportService;

    @Autowired
    private OperationProductInComponentCellBinderRegistry operationProductInComponentCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        operationProductInComponentXlsxImportService.downloadImportSchema(view, TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT, XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity technology = technologyForm.getEntity();
        technology = technology.getDataDefinition().save(technology);

        operationProductInComponentXlsxImportService.processImportFile(view,
                operationProductInComponentCellBinderRegistry.getCellBinderRegistry(), true,
                TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT, technology,
                OperationProductInComponentFields.TECHNOLOGY,
                OperationProductInComponentsImportListeners::createRestrictionForOperationProductInComponent);
    }

    private static SearchCriterion createRestrictionForOperationProductInComponent(final Entity operationProductInComponent) {
        return SearchRestrictions.eq(OperationProductInComponentFields.PRIORITY,
                operationProductInComponent.getStringField(OperationProductInComponentFields.PRIORITY));
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        operationProductInComponentXlsxImportService.redirectToLogs(view,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        operationProductInComponentXlsxImportService.changeButtonsState(view, false);
    }

}
