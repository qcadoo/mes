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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.Range;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.criteriaModifiers.ProductionLineCriteriaModifiersPFTD;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TOCDetailsHooksPFTD {

    private static final String L_FORM = "form";

    public void onBeforeRender(final ViewDefinitionState view) {
        setViewDisable(view);
        setCriteriaModifierParameters(view);
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent tocForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long tocId = tocForm.getEntityId();

        if (tocId == null) {
            return;
        }

        Entity division = tocForm.getPersistedEntityWithIncludedFormValues().getBelongsToField("division");
        LookupComponent productionLineLookupComponent = (LookupComponent) view.getComponentByReference("productionLine");
        FilterValueHolder productionLineLookupComponentHolder = productionLineLookupComponent.getFilterValue();

        if (division != null) {
            productionLineLookupComponentHolder
                    .put(ProductionLineCriteriaModifiersPFTD.DIVISION_PARAMETER, division.getId());
            productionLineLookupComponent.setFilterValue(productionLineLookupComponentHolder);
        } else {
            if (productionLineLookupComponentHolder.has(ProductionLineCriteriaModifiersPFTD.DIVISION_PARAMETER)) {
                productionLineLookupComponentHolder
                        .remove(ProductionLineCriteriaModifiersPFTD.DIVISION_PARAMETER);
                productionLineLookupComponent.setFilterValue(productionLineLookupComponentHolder);
            }
        }
    }

    private void setViewDisable(final ViewDefinitionState view) {
        FormComponent tocForm = (FormComponent) view.getComponentByReference(L_FORM);
        if (tocForm.getEntityId() == null) {
            return;
        }

        Entity technology = tocForm.getPersistedEntityWithIncludedFormValues().getBelongsToField(
                TechnologyOperationComponentFields.TECHNOLOGY);
        LookupComponent divisionookupComponent = (LookupComponent) view.getComponentByReference("division");
        LookupComponent plLookupComponent = (LookupComponent) view.getComponentByReference("productionLine");

        if (Range.ONE_DIVISION.getStringValue().equals(technology.getStringField(TechnologyFieldsPFTD.RANGE))) {
            divisionookupComponent.setEnabled(false);
            if (technology.getBelongsToField("productionLine") == null) {
                plLookupComponent.setEnabled(true);
            } else {
                plLookupComponent.setEnabled(false);
            }
        } else {
            divisionookupComponent.setEnabled(true);
            plLookupComponent.setEnabled(true);

        }
    }

    public void onDivisionChange(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

    }
}
