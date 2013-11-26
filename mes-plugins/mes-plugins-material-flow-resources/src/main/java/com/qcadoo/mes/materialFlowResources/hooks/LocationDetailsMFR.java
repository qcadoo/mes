/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class LocationDetailsMFR {

    private static final String L_LOCATION = "location";

    private static final String L_DIVISIONS = "divisions";

    private static final String L_FORM = "form";

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent locationForm = (FormComponent) view.getComponentByReference(L_FORM);
        GridComponent divisionsGrid = (GridComponent) view.getComponentByReference(L_DIVISIONS);

        Entity location = locationForm.getEntity();

        if (location.getId() != null) {
            FilterValueHolder filterValueHolder = divisionsGrid.getFilterValue();
            filterValueHolder.put(L_LOCATION, location.getId());

            divisionsGrid.setFilterValue(filterValueHolder);
        }
    }

}
