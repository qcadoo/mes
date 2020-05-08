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
package com.qcadoo.mes.technologiesGenerator.view;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Objects;
import java.util.Optional;

public class GeneratorView {

    private static final ImmutableSet<String> MAIN_TAB_COMPONENT_REFS = ImmutableSet.of("number", "product", "technology",
            "fetchTechnologiesForComponents", "createAndSwapProducts", "saved");

    private final ViewDefinitionState viewDefinitionState;

    private final FormComponent form;

    private final LookupComponent technologyLookup;

    private final TreeComponent treeComponent;

    private final WindowComponent window;

    public static GeneratorView from(final ViewDefinitionState view) {
        FormComponent form = view.<FormComponent> tryFindComponentByReference(QcadooViewConstants.L_FORM).orNull();
        Preconditions.checkArgument(form != null, "Cannot find form component!");

        LookupComponent technologyLookup = view.<LookupComponent> tryFindComponentByReference("technology").orNull();
        Preconditions.checkArgument(technologyLookup != null, "Cannot find technology lookup component!");

        TreeComponent treeComponent = (TreeComponent) view.getComponentByReference("technologiesTree");
        Preconditions.checkArgument(treeComponent != null, "Cannot find technologies tree component!");

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Preconditions.checkArgument(window != null, "Cannot find window component!");

        return new GeneratorView(view, window, form, technologyLookup, treeComponent);
    }

    GeneratorView(final ViewDefinitionState viewDefinitionState, final WindowComponent window, final FormComponent form,
            final LookupComponent technologyLookup, final TreeComponent treeComponent) {
        this.viewDefinitionState = viewDefinitionState;
        this.window = window;
        this.form = form;
        this.technologyLookup = technologyLookup;
        this.treeComponent = treeComponent;
    }

    public Entity getFormEntity() {
        return form.getPersistedEntityWithIncludedFormValues();
    }

    public void setFormEntity(final Entity entity) {
        form.setEntity(entity);
    }

    public Optional<Entity> getTechnology() {
        return Optional.ofNullable(technologyLookup.getEntity());
    }

    public Optional<Long> getSelectedNodeId() {
        return Optional.ofNullable(treeComponent.getSelectedEntityId());
    }

    public void redirectToTechnology(final Long id) {
        String url = "../page/technologies/technologyDetails.html";
        viewDefinitionState.redirectTo(url, false, true, ImmutableMap.of("form.id", id));
    }

    public void showErrorMsg(final String message) {
        form.addMessage(message, ComponentState.MessageType.FAILURE);
    }

    /**
     * Enables or disables the main tab & 'generate' ribbon button.
     *
     * @param enabled
     *            true, if the main tab & 'generate' button should be enabled. False otherwise.
     */
    public void setGenerationEnabled(final boolean enabled) {
        MAIN_TAB_COMPONENT_REFS.stream().map(viewDefinitionState::getComponentByReference).filter(Objects::nonNull)
                .forEach(comp -> comp.setEnabled(enabled));
        setGenerateRibbonButtonEnabled(enabled);
    }

    private void setGenerateRibbonButtonEnabled(final boolean enabled) {
        RibbonActionItem generateRibbonItem = getRibbonItemByName("generate");
        generateRibbonItem.setEnabled(enabled);
        generateRibbonItem.requestUpdate(true);
        window.requestRibbonRender();
    }

    public void setRefreshRibbonButtonEnabled(final boolean enabled) {
        RibbonActionItem generateRibbonItem = getRibbonItemByName("refresh");
        generateRibbonItem.setEnabled(enabled);
        generateRibbonItem.requestUpdate(true);
        window.requestRibbonRender();
    }

    private RibbonActionItem getRibbonItemByName(String itemName) {
        Ribbon ribbon = window.getRibbon();
        RibbonGroup generateGroup = ribbon.getGroupByName("generate");
        return generateGroup.getItemByName(itemName);
    }

    public void showStructureTreeTab() {
        window.setActiveTab("productStructure");
    }

    public void showGenerationSuccessMsg() {
        form.addMessage("technologiesGenerator.generate.success", ComponentState.MessageType.SUCCESS, true);
    }

    public void setActionsGroupButtonEnabled(boolean enabled, boolean msg, String itemName) {
        RibbonGroup actions = (RibbonGroup) window.getRibbon().getGroupByName("actions");
        RibbonActionItem saveButton = actions.getItemByName(itemName);
        saveButton.setEnabled(enabled);
        saveButton.requestUpdate(true);
        if(!enabled && msg) {
            saveButton.setMessage("technologiesGenerator.generateTechnologyInProgress");
        }
        window.requestRibbonRender();

    }

    public void setGenerationGroupButtonEnabled(boolean enabled, boolean msg, String itemName) {
        RibbonActionItem customizeTechnologiesRibbonItem = getRibbonItemByName(itemName);
        customizeTechnologiesRibbonItem.setEnabled(enabled);
        customizeTechnologiesRibbonItem.requestUpdate(true);
        if(!enabled && msg) {
            customizeTechnologiesRibbonItem.setMessage("technologiesGenerator.generateTechnologyInProgress");
        }
        window.requestRibbonRender();
    }
}
