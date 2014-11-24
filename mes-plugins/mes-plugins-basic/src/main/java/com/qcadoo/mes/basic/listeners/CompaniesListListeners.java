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
package com.qcadoo.mes.basic.listeners;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class CompaniesListListeners {

    private static final String L_GRID = "grid";

    private static final String L_ACTIONS = "actions";

    private static final String L_DELETE = "delete";

    @Autowired
    private CompanyService companyService;

    public void disabledRibbonForOwnerOrExternal(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        List<Entity> companies = grid.getSelectedEntities();

        boolean isEnabled = true;

        String buttonMessage = "basic.company.isOwner";

        for (Entity company : companies) {
            Entity companyFromDB = companyService.getCompany(company.getId());

            isEnabled = !companyService.isCompanyOwner(companyFromDB);

            if ((companyFromDB != null) && !StringUtils.isEmpty(company.getStringField(CompanyFields.EXTERNAL_NUMBER))) {
                buttonMessage = "basic.company.isExternalNumber";

                isEnabled = false;
            }

            if (!isEnabled) {
                break;
            }
        }

        companyService.disableButton(view, L_ACTIONS, L_DELETE, isEnabled, buttonMessage);
    }

}
