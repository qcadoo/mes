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
package com.qcadoo.mes.advancedGenealogy.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordStateChangeFields;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;

@Service
public class TrackingRecordSimpleViewHooks {

    private static final String L_LOGGINGS_GRID = "loggingsGrid";

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_TYPE_01SIMPLE = "01simple";

    private static final String L_PRODUCT = "product";

    private static final String L_UNIT = "unit";

    private static final String L_BATCH_LOOKUP = "batchLookup";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void generateOrderNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                AdvancedGenealogyConstants.MODEL_TRACKING_RECORD, QcadooViewConstants.L_FORM, TrackingRecordFields.NUMBER);
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference(L_LOGGINGS_GRID);
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService
                .buildStatusRestriction(TrackingRecordStateChangeFields.STATUS, Lists.newArrayList(SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public final void fillUnitField(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        fillUnitField(view);
    }

    public final void fillUnitField(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent batchLookup = getFieldComponent(view, L_BATCH_LOOKUP);
        FieldComponent unitField = getFieldComponent(view, L_UNIT);

        if (form.getEntityId() == null) {
            return;
        }
        if (batchLookup.getFieldValue() == null) {
            unitField.setFieldValue(null);
            return;
        }

        Long batchId = Long.valueOf(batchLookup.getFieldValue().toString());
        Entity batch = getDataDef(AdvancedGenealogyConstants.MODEL_BATCH)
                .get(batchId);
        Entity trackingRecord = getDataDef(
                AdvancedGenealogyConstants.MODEL_TRACKING_RECORD).get(form.getEntityId());
        Entity savedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);

        if (batch == null) {
            unitField.setFieldValue(null);
            return;
        }

        grid.setEditable(savedBatch != null && batchId.equals(savedBatch.getId()));

        Entity product = batch.getBelongsToField(L_PRODUCT);
        unitField.setFieldValue(product.getField(L_UNIT));
    }

    public final void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        grid.setCustomRestriction(searchBuilder -> searchBuilder.add(SearchRestrictions.eq(L_ENTITY_TYPE, L_TYPE_01SIMPLE)));
    }

    public final void setEntityTypeToSimple(final ViewDefinitionState view) {
        FieldComponent entityType = getFieldComponent(view, L_ENTITY_TYPE);
        entityType.setFieldValue(L_TYPE_01SIMPLE);
        entityType.requestComponentUpdateState();
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

    private DataDefinition getDataDef(final String modelName) {
        return dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, modelName);
    }

}
