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
package com.qcadoo.mes.technologiesGenerator.hooks;

import com.qcadoo.mes.technologiesGenerator.constants.GeneratorContextFields;
import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.mes.technologiesGenerator.criteriaModifier.TechnologiesForProductsCM;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyStructureNodeDataProvider;
import com.qcadoo.mes.technologiesGenerator.view.GeneratorView;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GeneratorViewHooks {

    

    @Autowired
    private TechnologyStructureNodeDataProvider nodeDataProvider;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        GeneratorView generatorView = GeneratorView.from(view);
        generateGeneratorNumber(view);
        showRibbonButtons(generatorView, view);
        GridComponent grid = (GridComponent) view.getComponentByReference("generatorTechnologiesForProducts");
        FilterValueHolder gridHolder = grid.getFilterValue();
        gridHolder.put(TechnologiesForProductsCM.PARAMETER, generatorView.getFormEntity().getId());
        grid.setFilterValue(gridHolder);

    }

    public void generateGeneratorNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER, TechnologiesGeneratorConstants.MODEL_GENERATOR_CONTEXT,
                QcadooViewConstants.L_FORM, GeneratorContextFields.NAME);
    }

    void showRibbonButtons(final GeneratorView generatorView, final ViewDefinitionState view) {
        Entity contextEntity = generatorView.getFormEntity();
        boolean isAlreadyGenerated = contextEntity.getBooleanField(GeneratorContextFields.GENERATED);
        boolean generationInProgress = contextEntity.getBooleanField(GeneratorContextFields.GENERATION_IN_PROGRSS);
        generatorView.setGenerationEnabled(!isAlreadyGenerated);
        generatorView.setRefreshRibbonButtonEnabled(isAlreadyGenerated);
        Entity context = generatorView.getFormEntity();
        if (context.getId() != null) {
            List<Entity> products = context.getHasManyField(GeneratorContextFields.PRODUCTS);
            Optional<List<Entity>> optionalNodes = nodeDataProvider.getCastumizedNodesForContext(context);
            if (optionalNodes.get().isEmpty() || products.isEmpty()) {
                generatorView.setGenerationGroupButtonEnabled(false, false, "generateTechnologies");

            } else {
                generatorView.setGenerationGroupButtonEnabled(!generationInProgress, true, "generateTechnologies");
                generatorView.setGenerationGroupButtonEnabled(!generationInProgress, true, "refresh");
                generatorView.setGenerationGroupButtonEnabled(!generationInProgress, true, "customize");
                generatorView.setActionsGroupButtonEnabled(!generationInProgress, true, "save");
                generatorView.setActionsGroupButtonEnabled(!generationInProgress, true, "delete");
            }
        } else {
            generatorView.setGenerationGroupButtonEnabled(false, false, "generateTechnologies");

        }
    }

}
