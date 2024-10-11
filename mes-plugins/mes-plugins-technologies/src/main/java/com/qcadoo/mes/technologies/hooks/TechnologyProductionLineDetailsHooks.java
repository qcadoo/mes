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
package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.mes.technologies.criteriaModifiers.TechnologyProductionLineCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TechnologyProductionLineDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference(TechnologyProductionLineFields.PRODUCTION_LINE);

        Entity entity = form.getEntity();
        Entity technology = entity.getBelongsToField(TechnologyProductionLineFields.TECHNOLOGY);
        Entity division = technology.getBelongsToField(TechnologyFields.DIVISION);

        FilterValueHolder filterValueHolder = productionLineLookup.getFilterValue();

        Long technologyId = technology.getId();

        if (Objects.isNull(technologyId)) {
            filterValueHolder.remove(TechnologyProductionLineCriteriaModifiers.L_TECHNOLOGY_ID);
        } else {
            filterValueHolder.put(TechnologyProductionLineCriteriaModifiers.L_TECHNOLOGY_ID, technologyId);
        }

        if (Objects.nonNull(division)) {
            filterValueHolder.put(TechnologyProductionLineCriteriaModifiers.L_DIVISION_ID, division.getId());
        } else if (filterValueHolder.has(TechnologyProductionLineCriteriaModifiers.L_DIVISION_ID)) {
            filterValueHolder.remove(TechnologyProductionLineCriteriaModifiers.L_DIVISION_ID);
        }

        productionLineLookup.setFilterValue(filterValueHolder);
    }

}
