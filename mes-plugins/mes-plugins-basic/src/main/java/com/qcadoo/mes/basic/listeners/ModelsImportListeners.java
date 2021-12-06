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
package com.qcadoo.mes.basic.listeners;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ModelFields;
import com.qcadoo.mes.basic.imports.model.ModelCellBinderRegistry;
import com.qcadoo.mes.basic.imports.model.ModelXlsxImportService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ModelsImportListeners {

    @Autowired
    private ModelXlsxImportService modelXlsxImportService;

    @Autowired
    private ModelCellBinderRegistry modelCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        modelXlsxImportService.downloadImportSchema(view, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_MODEL,
                XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        modelXlsxImportService.processImportFile(view, modelCellBinderRegistry.getCellBinderRegistry(), true,
                BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_MODEL,
                ModelsImportListeners::createRestrictionForModel);
    }

    private static SearchCriterion createRestrictionForModel(final Entity model) {
        return SearchRestrictions.eq(ModelFields.NAME, model.getStringField(ModelFields.NAME));
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        modelXlsxImportService.redirectToLogs(view, BasicConstants.MODEL_MODEL);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        modelXlsxImportService.changeButtonsState(view, false);
    }

}
