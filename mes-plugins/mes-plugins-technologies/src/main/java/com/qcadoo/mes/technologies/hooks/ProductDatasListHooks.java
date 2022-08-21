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
package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.SelectedTechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.criteriaModifiers.ProductDataCriteriaModifiers;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductDatasListHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setFilter(view);
    }

    private void setFilter(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> selected = getSelectedTechnologies();

        if (selected.size() > 0) {
            Long technologyId = selected.get(0).getIntegerField(SelectedTechnologyFields.TECHNOLOGY_ID).longValue();

            FilterValueHolder filter = grid.getFilterValue();
            filter.put(ProductDataCriteriaModifiers.FILTER_KEY_TECHNOLOGY_ID, technologyId);

            grid.setFilterValue(filter);
        }

        deleteSelectedTechnology();
    }

    private void deleteSelectedTechnology() {
        DataDefinition selectedTechnologyDD = getSelectedTechnologyDD();

        getSelectedTechnologies().forEach(selectedTechnology -> selectedTechnologyDD.delete(selectedTechnology.getId()));
    }

    private List<Entity> getSelectedTechnologies() {
        return getSelectedTechnologyDD().find().list().getEntities();
    }

    private DataDefinition getSelectedTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_SELECTED_TECHNOLOGY);
    }

}
