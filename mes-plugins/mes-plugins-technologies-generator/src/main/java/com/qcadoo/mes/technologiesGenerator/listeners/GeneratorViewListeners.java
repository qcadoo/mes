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
package com.qcadoo.mes.technologiesGenerator.listeners;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.TechnologiesGeneratorForProductsService;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorContextFields;
import com.qcadoo.mes.technologiesGenerator.customization.technology.TechnologyCustomizer;
import com.qcadoo.mes.technologiesGenerator.domain.ContextId;
import com.qcadoo.mes.technologiesGenerator.tree.TreeGenerator;
import com.qcadoo.mes.technologiesGenerator.view.GeneratorView;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneratorViewListeners {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorViewListeners.class);

    @Autowired
    private TreeGenerator treeGenerator;

    @Autowired
    private TechnologyCustomizer technologyCustomizer;

    @Autowired
    private TechnologiesGeneratorForProductsService technologiesGeneratorForProductsService;

    @Autowired
    private ParameterService parameterService;

    public void goToGeneratedTechnologies(final ViewDefinitionState view, final ComponentState eventPerformer, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("generatorTechnologiesForProducts");
        if(!grid.getSelectedEntitiesIds().isEmpty()) {
            String url = "../page/technologies/technologyDetails.html";
            Entity e = grid.getSelectedEntities().get(0).getDataDefinition().get(grid.getSelectedEntities().get(0).getId());

            view.redirectTo(url, false, true, ImmutableMap.of("form.id", e.getBelongsToField("technology").getId()));
        }
    }

    public void generateTechnologies(final ViewDefinitionState view, final ComponentState eventPerformer, final String[] args) {
        GeneratorView generatorView = GeneratorView.from(view);
        Entity context  = generatorView.getFormEntity().getDataDefinition().get(generatorView.getFormEntity().getId());
        context.setField(GeneratorContextFields.GENERATION_IN_PROGRSS, true);
        context = context.getDataDefinition().save(context);
        generatorView.setFormEntity(context);
        view.addMessage("generator.technology.generationTechnologyStart", ComponentState.MessageType.INFO, false);
        technologiesGeneratorForProductsService.performGeneration(generatorView);
    }

    public void generateTree(final ViewDefinitionState view, final ComponentState eventPerformer, final String[] args) {
        GeneratorView generatorView = GeneratorView.from(view);
        Either<String, ContextId> generationResults = generate(generatorView, false);
        if (generationResults.isRight()) {
            generatorView.showStructureTreeTab();
        }
    }

    public void refreshTree(final ViewDefinitionState view, final ComponentState eventPerformer, final String[] args) {
        generate(GeneratorView.from(view), false);
    }

    public void refreshAndApplyCustomized(final ViewDefinitionState view, final ComponentState eventPerformer, final String[] args) {
        generate(GeneratorView.from(view), true);
    }

    private Either<String, ContextId> generate(final GeneratorView generatorView, boolean applyCustomized) {
        Entity context = generatorView.getFormEntity();

        Entity savedContext = context.getDataDefinition().save(context);
        if (savedContext.isValid()) {
            return performGeneration(generatorView, savedContext, applyCustomized);
        }
        generatorView.setFormEntity(savedContext);
        return Either.left("Context has validation errors");
    }

    private Either<String, ContextId> performGeneration(final GeneratorView generatorView, final Entity savedContext,
            boolean applyCustomized) {
        Either<String, ContextId> generationRes = treeGenerator.generate(savedContext, GeneratorSettings.from(savedContext, parameterService.getParameter()), applyCustomized);
        if (generationRes.isRight()) {
            Entity updatedContext = savedContext.getDataDefinition().get(savedContext.getId());
            generatorView.setFormEntity(updatedContext);
            generatorView.showGenerationSuccessMsg();
        } else {
            generatorView.showErrorMsg("technologiesGenerator.generate.error.problemDuringStructureGeneration");
        }
        return generationRes;
    }

    public void customize(final ViewDefinitionState view, final ComponentState eventPerformer, final String[] args) {
        customize(GeneratorView.from(view));
    }

    private void customize(final GeneratorView generatorView) {
        Entity context = generatorView.getFormEntity();
        Optional<Either<String, TechnologyId>> customizedTechId = tryPerformCustomization(generatorView, context);
        customizedTechId.ifPresent(res -> {
            if (res.isLeft()) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(res.getLeft());
                }
                generatorView.showErrorMsg("technologiesGenerator.generate.error.problemDuringCustomization");
            } else {
                generatorView.redirectToTechnology(res.getRight().get());
            }
        });
    }

    private Optional<Either<String, TechnologyId>> tryPerformCustomization(final GeneratorView generatorView, final Entity context) {
        try {
            Optional<Either<String, TechnologyId>> customizedTechId =  Optional.ofNullable(context.getBelongsToField(GeneratorContextFields.PRODUCT)).flatMap(
                    mainProduct -> generatorView.getSelectedNodeId()
                            .flatMap(nodeId -> technologyCustomizer
                                            .customize(nodeId, mainProduct, GeneratorSettings.from(context, parameterService.getParameter()), false)));
            customizedTechId.ifPresent(cti -> {
                if (cti.isRight()) {
                    TechnologyId technologyId = cti.getRight();
                    technologyCustomizer.addCustomizedProductToQualityCard(technologyId);
                }
            });
            return customizedTechId;
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Cannot perform technology customization due to unexpected error", e);
            }
            return Optional.of(Either.left("Cannot perform technology customization due to unexpected error"));
        }
    }


}
