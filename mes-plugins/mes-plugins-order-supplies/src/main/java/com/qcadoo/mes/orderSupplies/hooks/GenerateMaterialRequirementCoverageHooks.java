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
package com.qcadoo.mes.orderSupplies.hooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import com.qcadoo.view.constants.RowStyle;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GenerateMaterialRequirementCoverageHooks {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateMaterialRequirementCoverageHooks.class);

    private static final String L_COVERAGE = "coverage";

    private static final String L_REPORTS = "reports";

    private static final String L_ACTIONS = "actions";

    private static final String L_SHOW_MATERIAL_REQUIREMENT_COVERAGES = "showMaterialRequirementCoverages";

    private static final String L_SAVE_MATERIAL_REQUIREMENT_COVERAGE = "saveMaterialRequirementCoverage";

    private static final String L_PRINT_MATERIAL_REQUIREMENT_COVERAGE = "printMaterialRequirementCoverage";

    private static final String L_GENERATE_MATERIAL_REQUIREMENT_COVERAGE = "generateMaterialRequirementCoverage";

    private static final String L_ADD_MULTI = "addMulti";

    public static final String L_MATERIAL_AVAILABILITY = "materialAvailability";

    public static final String L_COVERAGE_PRODUCTS = "coverageProducts";

    public static final String L_SHOW_REPLACEMENTS_AVAILABILITY = "showReplacementsAvailability";

    public static final String L_REPLACEMENT = "replacement";
    public static final String L_ANALYSIS = "analysis";
    public static final String L_GENERATE_COVERAGE_ANALYSIS = "generateCoverageAnalysis";

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private DataDefinition coverageProductGeneratedDataDefinition;

    public void init() {
        coverageProductGeneratedDataDefinition = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                CoverageProductGeneratedFields.ENTITY_NAME);
    }

    public void onBeforeRender(final ViewDefinitionState view) {
        generateMaterialRequirementCoverageNumber(view);
        fillActualDate(view);
        fillCoverageDate(view);
        fillDefaultValuesFromParameter(view);
        updateFormState(view);
        updateRibbonState(view);
        updateMRCCriteriaModifiersState(view);
        hideTabInData(view);
    }

    public void generateMaterialRequirementCoverageNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE, QcadooViewConstants.L_FORM, MaterialRequirementCoverageFields.NUMBER);
    }

    public void fillActualDate(final ViewDefinitionState view) {
        FormComponent mateiralRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (mateiralRequirementCoverageForm.getEntityId() != null) {
            return;
        }

        FieldComponent actualDateField = (FieldComponent) view
                .getComponentByReference(MaterialRequirementCoverageFields.ACTUAL_DATE);
        String actualDate = (String) actualDateField.getFieldValue();

        if (StringUtils.isEmpty(actualDate)) {
            actualDateField.setFieldValue(DateUtils.toDateTimeString(new Date()));
            actualDateField.requestComponentUpdateState();
        }
    }

    public void fillCoverageDate(final ViewDefinitionState view) {
        FormComponent materialRequirementCoverageform = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (materialRequirementCoverageform.getEntityId() != null) {
            return;
        }

        FieldComponent coverageDateField = (FieldComponent) view
                .getComponentByReference(MaterialRequirementCoverageFields.COVERAGE_TO_DATE);
        String coverageDate = (String) coverageDateField.getFieldValue();

        Entity parameter = parameterService.getParameter();
        Integer defaultCoverageFromDays = parameter.getIntegerField(ParameterFieldsOS.DEFAULT_COVERAGE_FROM_DAYS);

        if (StringUtils.isEmpty(coverageDate) && defaultCoverageFromDays != null) {
            Date coverageToDate = new DateTime().plusDays(defaultCoverageFromDays).toDate();
            coverageDateField.setFieldValue(DateUtils.toDateTimeString(coverageToDate));
        }
        coverageDateField.requestComponentUpdateState();
    }

    public void updateFormState(final ViewDefinitionState view) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view
                .getComponentByReference(MaterialRequirementCoverageFields.COVERAGE_LOCATIONS);

        boolean saved = orderSuppliesService.checkIfMaterialRequirementCoverageIsSaved(materialRequirementCoverageId);

        materialRequirementCoverageForm.setFormEnabled(!saved);

        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            formComponent.setFormEnabled(!saved);
        }
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

        boolean saved = orderSuppliesService.checkIfMaterialRequirementCoverageIsSaved(materialRequirementCoverageId);

        FieldComponent generatedField = (FieldComponent) view
                .getComponentByReference(MaterialRequirementCoverageFields.GENERATED);
        boolean generated = "1".equals(generatedField.getFieldValue());

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup coverage = (RibbonGroup) window.getRibbon().getGroupByName(L_COVERAGE);
        RibbonGroup reports = (RibbonGroup) window.getRibbon().getGroupByName(L_REPORTS);
        RibbonGroup materialAvailability = (RibbonGroup) window.getRibbon().getGroupByName(L_MATERIAL_AVAILABILITY);
        RibbonGroup analysis = (RibbonGroup) window.getRibbon().getGroupByName(L_ANALYSIS);

        RibbonActionItem generateMaterialRequirementCoverage = (RibbonActionItem) coverage
                .getItemByName(L_GENERATE_MATERIAL_REQUIREMENT_COVERAGE);
        RibbonActionItem printMaterialRequirementCoverage = (RibbonActionItem) coverage
                .getItemByName(L_PRINT_MATERIAL_REQUIREMENT_COVERAGE);
        RibbonActionItem saveMaterialRequirementCoverage = (RibbonActionItem) reports
                .getItemByName(L_SAVE_MATERIAL_REQUIREMENT_COVERAGE);
        RibbonActionItem showMaterialRequirementCoverages = (RibbonActionItem) reports
                .getItemByName(L_SHOW_MATERIAL_REQUIREMENT_COVERAGES);
        RibbonActionItem showReplacementsAvailability = (RibbonActionItem) materialAvailability
                .getItemByName(L_SHOW_REPLACEMENTS_AVAILABILITY);

        RibbonActionItem generateCoverageAnalysis = (RibbonActionItem) analysis
                .getItemByName(L_GENERATE_COVERAGE_ANALYSIS);

        showReplacementsAvailability
                .setMessage("orderWithMaterialAvailabilityList.materialAvailability.ribbon.message.selectOneRecordWithReplacements");
        boolean areSaved = checkIfThereAreSavedMaterialRequirementCoverages();

        updateButtonState(generateMaterialRequirementCoverage, !saved);
        updateButtonState(printMaterialRequirementCoverage, generated && !saved);
        updateButtonState(saveMaterialRequirementCoverage, generated && !saved);
        updateButtonState(showMaterialRequirementCoverages, areSaved);

        GridComponent grid = (GridComponent) view.getComponentByReference(L_COVERAGE_PRODUCTS);
        if (generated && grid.getSelectedEntitiesIds().size() == 1
                && isReplacement(grid.getSelectedEntitiesIds().stream().findFirst().get())) {
            showReplacementsAvailability.setEnabled(true);
        } else {
            showReplacementsAvailability.setEnabled(false);
        }
        showReplacementsAvailability.requestUpdate(true);

        if (generated) {
            generateCoverageAnalysis.setEnabled(true);
        } else {
            generateCoverageAnalysis.setEnabled(false);
        }
        generateCoverageAnalysis.requestUpdate(true);
    }

    private boolean isReplacement(Long coverageProductId) {
        Entity pc = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT_DTO).get(coverageProductId);
        if (Objects.isNull(pc)) {
            return false;
        }
        return pc.getBooleanField(L_REPLACEMENT);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private boolean checkIfThereAreSavedMaterialRequirementCoverages() {
        SearchResult searchResult = orderSuppliesService.getMaterialRequirementCoverageDD()
                .find("select mrc.id from #orderSupplies_materialRequirementCoverage mrc where mrc.saved = true").list();

        return !searchResult.getEntities().isEmpty();
    }

    public void fillDefaultValuesFromParameter(final ViewDefinitionState view) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (materialRequirementCoverageForm.getEntityId() != null) {
            return;
        }

        Entity parameter = parameterService.getParameter();

        if (StringUtils.isNotEmpty(parameter.getStringField(ParameterFieldsOS.COVERAGE_TYPE))) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(ParameterFieldsOS.COVERAGE_TYPE);

            field.setFieldValue(parameter.getStringField(ParameterFieldsOS.COVERAGE_TYPE));
            field.requestComponentUpdateState();
        }

        if (StringUtils.isNotEmpty(parameter.getStringField(ParameterFieldsD.INCLUDE_IN_CALCULATION_DELIVERIES))) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(ParameterFieldsD.INCLUDE_IN_CALCULATION_DELIVERIES);

            field.setFieldValue(parameter.getStringField(ParameterFieldsD.INCLUDE_IN_CALCULATION_DELIVERIES));
            field.requestComponentUpdateState();
        }

        if (parameter.getBooleanField(ParameterFieldsOS.AUTOMATIC_SAVE_COVERAGE)) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(ParameterFieldsOS.AUTOMATIC_SAVE_COVERAGE);

            field.setFieldValue(parameter.getBooleanField(ParameterFieldsOS.AUTOMATIC_SAVE_COVERAGE));
            field.requestComponentUpdateState();
        }

        copyLocationFromParameter(view, parameter);
        copyOrderStatesFromParameter(view, parameter);
    }

    private void updateMRCCriteriaModifiersState(final ViewDefinitionState view) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (materialRequirementCoverageForm.getEntityId() == null) {
            return;
        }

        GridComponent gridComponent = (GridComponent) view.getComponentByReference("coverageProducts");
        FilterValueHolder filterValueHolder = gridComponent.getFilterValue();
        filterValueHolder.put("coverageOrdersSelected", materialRequirementCoverageForm
                .getPersistedEntityWithIncludedFormValues().getHasManyField("coverageOrders").size() > 0);
        gridComponent.setFilterValue(filterValueHolder);
    }

    private void hideTabInData(ViewDefinitionState view) {
        FormComponent coverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity coverageEntity = coverageForm.getEntity();
        Entity order = coverageEntity.getBelongsToField("order");

        view.getComponentByReference("window.inDataTab").setVisible(order == null);
    }

    private void copyOrderStatesFromParameter(final ViewDefinitionState view, final Entity parameter) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view
                .getComponentByReference(MaterialRequirementCoverageFields.COVERAGE_ORDER_STATES);

        List<FormComponent> formComponents = adl.getFormComponents();

        if (formComponents.isEmpty()) {
            List<Entity> coverageOrderStates = Lists.newArrayList();

            List<Entity> parameterCoverageOrderStates = parameter.getHasManyField(ParameterFieldsOS.COVERAGE_ORDER_STATES);

            for (Entity parameterCoverageOrderState : parameterCoverageOrderStates) {
                String state = parameterCoverageOrderState.getStringField(CoverageOrderStateFields.STATE);

                Entity coverageOrderState = orderSuppliesService.getCoverageOrderStateDD().create();

                coverageOrderState.setField(CoverageOrderStateFields.STATE, state);

                coverageOrderStates.add(coverageOrderState);
            }

            adl.setFieldValue(coverageOrderStates);
            adl.requestComponentUpdateState();
        }
    }

    private void copyLocationFromParameter(final ViewDefinitionState view, final Entity parameter) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view
                .getComponentByReference(MaterialRequirementCoverageFields.COVERAGE_LOCATIONS);

        List<FormComponent> formComponents = adlc.getFormComponents();

        if (formComponents.isEmpty()) {
            List<Entity> coverageLocations = Lists.newArrayList();

            List<Entity> parameterCoverageLocations = parameter.getHasManyField(ParameterFieldsOS.COVERAGE_LOCATIONS);

            for (Entity parameterCoverageLocation : parameterCoverageLocations) {
                Entity location = parameterCoverageLocation.getBelongsToField(CoverageLocationFields.LOCATION);

                Entity coverageLocation = orderSuppliesService.getCoverageLocationDD().create();

                coverageLocation.setField(CoverageLocationFields.LOCATION, location);

                coverageLocations.add(coverageLocation);
            }

            adlc.setFieldValue(coverageLocations);
            adlc.requestComponentUpdateState();
        }
    }

    /**
     * Marking rows as based on generated coverage products table
     *
     * @param entity
     * @return
     */
    public Set<String> fillRowStylesBasedOnGenerated(final Entity entity) {
        init();
        Entity product = entity.getBelongsToField("product");
        Set<String> rowStyles = Sets.newHashSet();

        Set<Long> generatedIds = coverageProductGeneratedDataDefinition.find().list().getEntities().stream()
                .map(generated -> generated.getIntegerField("productId").longValue()).collect(Collectors.toSet());

        if (generatedIds.contains(product.getId())) {
            rowStyles.add(RowStyle.RED_BACKGROUND);
        } else {
            rowStyles.add("");
        }

        return rowStyles;
    }

}
