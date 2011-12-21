/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
package com.qcadoo.mes.workPlans;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;

@Service
public final class WorkPlanService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    void generateWorkPlanDocuments(final ComponentState state, final Entity workPlan) throws IOException, DocumentException {
        // Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
        // .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        // workPlanForMachinePdfService.generateDocument(workPlanWithFileName, company, state.getLocale());
        // workPlanForWorkerPdfService.generateDocument(workPlanWithFileName, company, state.getLocale());
        // workPlanForProductPdfService.generateDocument(workPlanWithFileName, company, state.getLocale());
    }

    public Entity getWorkPlan(final Long workPlanId) {
        return getWorkPlanDataDefinition().get(workPlanId);
    }

    private DataDefinition getWorkPlanDataDefinition() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN);
    }
}
