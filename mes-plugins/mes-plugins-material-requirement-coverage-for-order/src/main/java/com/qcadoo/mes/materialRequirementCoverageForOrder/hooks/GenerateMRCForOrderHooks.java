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
package com.qcadoo.mes.materialRequirementCoverageForOrder.hooks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialRequirementCoverageForOrder.MaterialRequirementCoverageForOrderService;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageForOrderFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageLocationFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.MaterialRequirementCoverageForOrderConstans;
import com.qcadoo.mes.orderSupplies.constants.ParameterFieldsOS;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productFlowThruDivision.ProductFlowThruDivisionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class GenerateMRCForOrderHooks {

    



    private static final String L_COVERAGE = "coverage";

    private static final String L_REPORTS = "reports";

    private static final String L_SHOW_MATERIAL_REQUIREMENT_COVERAGES = "showMaterialRequirementCoverages";

    private static final String L_SAVE_MATERIAL_REQUIREMENT_COVERAGE = "saveMaterialRequirementCoverage";

    private static final String L_PRINT_MATERIAL_REQUIREMENT_COVERAGE = "printMaterialRequirementCoverage";

    private static final String L_GENERATE_MATERIAL_REQUIREMENT_COVERAGE = "generateMaterialRequirementCoverage";

    @Autowired
    private MaterialRequirementCoverageForOrderService mRCForOrderService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductFlowThruDivisionService productFlowThruDivisionService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        fillFieldsForOrder(view);
        generateMaterialRequirementCoverageNumber(view);
        fillActualDate(view);
        setLocations(view);
        updateRibbonState(view);
        updateFormState(view);
    }

    private void setLocations(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity mRCForOrder = form.getEntity();

        Entity order = null;
        Set<Entity> locations = Sets.newHashSet();
        order = mRCForOrder.getBelongsToField("order");
        locations = productFlowThruDivisionService.getProductsLocations(order.getBelongsToField(OrderFields.TECHNOLOGY).getId());
        List<Entity> parameterCoverageLocations = parameterService.getParameter().getHasManyField(
                ParameterFieldsOS.COVERAGE_LOCATIONS);

        for (Entity parameterCoverageLocation : parameterCoverageLocations) {
            Entity location = parameterCoverageLocation.getBelongsToField(
                    com.qcadoo.mes.orderSupplies.constants.CoverageLocationFields.LOCATION);
            locations.add(location);
        }
        AwesomeDynamicListComponent locationADL = (AwesomeDynamicListComponent) view.getComponentByReference("coverageLocations");
        if (locationADL.getFormComponents().isEmpty()) {
            List<Entity> locationsList = Lists.newArrayList();

            for (Entity location : locations) {

                Entity coverageLocation = mRCForOrderService.getCoverageLocationDD().create();

                coverageLocation.setField(CoverageLocationFields.LOCATION, location);

                locationsList.add(coverageLocation);
            }
            locationADL.setFieldValue(locationsList);

            locationADL.requestComponentUpdateState();
        }

    }

    private void fillFieldsForOrder(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity mRCForOrder = form.getEntity();

        Entity order = null;

        if (mRCForOrder.getId() == null) {
            order = mRCForOrder.getBelongsToField("order");
            if (order.getDateField(OrderFields.DATE_FROM) == null) {
                return;
            }
            FieldComponent coverageToDate = (FieldComponent) view.getComponentByReference("coverageToDate");
            if (coverageToDate.getFieldValue() == null) {
                coverageToDate.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, LocaleContextHolder.getLocale())
                        .format((order.getDateField(OrderFields.DATE_FROM))));
            }
        }

    }

    public void generateMaterialRequirementCoverageNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, MaterialRequirementCoverageForOrderConstans.PLUGIN_IDENTIFIER,
                MaterialRequirementCoverageForOrderConstans.MODEL_COVERAGE_FOR_ORDER, QcadooViewConstants.L_FORM, CoverageForOrderFields.NUMBER);
    }

    public void fillActualDate(final ViewDefinitionState view) {
        FormComponent mateiralRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (mateiralRequirementCoverageForm.getEntityId() != null) {
            return;
        }

        FieldComponent actualDateField = (FieldComponent) view.getComponentByReference(CoverageForOrderFields.ACTUAL_DATE);
        String actualDate = (String) actualDateField.getFieldValue();

        if (StringUtils.isEmpty(actualDate)) {
            actualDateField.setFieldValue(DateUtils.toDateTimeString(new Date()));
            actualDateField.requestComponentUpdateState();
        }
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

        Entity mRCForOrder = materialRequirementCoverageForm.getEntity();

        Entity order = mRCForOrder.getBelongsToField("order");

        boolean saved = checkIfMaterialRequirementCoverageIsSaved(materialRequirementCoverageId);

        boolean enable = false;
        if (order.getStringField(OrderFields.STATE).equals(OrderState.ACCEPTED.getStringValue())) {
            enable = true;
        }

        FieldComponent generatedField = (FieldComponent) view.getComponentByReference(CoverageForOrderFields.GENERATED);
        boolean generated = "1".equals(generatedField.getFieldValue());

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup coverage = (RibbonGroup) window.getRibbon().getGroupByName(L_COVERAGE);
        RibbonGroup reports = (RibbonGroup) window.getRibbon().getGroupByName(L_REPORTS);

        RibbonActionItem generateMaterialRequirementCoverage = (RibbonActionItem) coverage
                .getItemByName(L_GENERATE_MATERIAL_REQUIREMENT_COVERAGE);
        RibbonActionItem printMaterialRequirementCoverage = (RibbonActionItem) coverage
                .getItemByName(L_PRINT_MATERIAL_REQUIREMENT_COVERAGE);
        RibbonActionItem saveMaterialRequirementCoverage = (RibbonActionItem) reports
                .getItemByName(L_SAVE_MATERIAL_REQUIREMENT_COVERAGE);
        RibbonActionItem showMaterialRequirementCoverages = (RibbonActionItem) reports
                .getItemByName(L_SHOW_MATERIAL_REQUIREMENT_COVERAGES);

        boolean areSaved = checkIfThereAreSavedMaterialRequirementCoverages();

        updateButtonState(generateMaterialRequirementCoverage, enable);
        updateButtonState(printMaterialRequirementCoverage, generated && !saved);
        updateButtonState(saveMaterialRequirementCoverage, generated && !saved);
        updateButtonState(showMaterialRequirementCoverages, areSaved);
    }

    public boolean checkIfMaterialRequirementCoverageIsSaved(final Long materialRequirementCoverageId) {
        if (materialRequirementCoverageId != null) {
            Entity materialRequirementCoverage = mRCForOrderService.getMRCForOrder(materialRequirementCoverageId);

            if (materialRequirementCoverage != null) {
                return materialRequirementCoverage.getBooleanField(CoverageForOrderFields.SAVED);
            }
        }

        return false;
    }

    private boolean checkIfThereAreSavedMaterialRequirementCoverages() {
        SearchResult searchResult = mRCForOrderService.getMRCDD().find()
                .add(SearchRestrictions.eq(CoverageForOrderFields.SAVED, true)).list();

        return !searchResult.getEntities().isEmpty();
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void updateFormState(final ViewDefinitionState view) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view
                .getComponentByReference(CoverageForOrderFields.COVERAGE_LOCATIONS);

        boolean saved = checkIfMaterialRequirementCoverageIsSaved(materialRequirementCoverageId);

        materialRequirementCoverageForm.setFormEnabled(!saved);

        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            formComponent.setFormEnabled(!saved);
        }
    }
}
