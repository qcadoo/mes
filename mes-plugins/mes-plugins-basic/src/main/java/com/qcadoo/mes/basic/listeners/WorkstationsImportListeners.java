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
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.basic.imports.workstation.WorkstationCellBinderRegistry;
import com.qcadoo.mes.basic.imports.workstation.WorkstationXlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class WorkstationsImportListeners {

    @Autowired
    private WorkstationXlsxImportService workstationXlsxImportService;

    @Autowired
    private WorkstationCellBinderRegistry workstationCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        workstationXlsxImportService.downloadImportSchema(view, BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_WORKSTATION, XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        workstationXlsxImportService.processImportFile(view, workstationCellBinderRegistry.getCellBinderRegistry(), true,
                BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION,
                WorkstationsImportListeners::createRestrictionForWorkstation);
    }

    private static SearchCriterion createRestrictionForWorkstation(final Entity workstation) {
        return SearchRestrictions.eq(WorkstationFields.NUMBER, workstation.getStringField(WorkstationFields.NUMBER));
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        workstationXlsxImportService.redirectToLogs(view, BasicConstants.MODEL_WORKSTATION);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        workstationXlsxImportService.changeButtonsState(view, false);
    }

}
