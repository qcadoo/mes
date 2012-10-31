/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.genealogies.print;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ReportService {

    public void changeProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }

        GridComponent batches = (GridComponent) viewDefinitionState.getComponentByReference("batches");

        batches.setSelectedEntitiesIds(new HashSet<Long>());
    }

    public void addRestrictionToGenealogyGrid(final ViewDefinitionState viewDefinitionState) {
        final FieldComponent product = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        final GridComponent batches = (GridComponent) viewDefinitionState.getComponentByReference("batches");

        batches.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchCriteriaBuilder) {
                searchCriteriaBuilder.belongsTo("order.product", product.getFieldValue());
            }

        });

        batches.setEntities(setDistinctBatch(batches.getEntities()));
    }

    public List<Entity> setDistinctBatch(final List<Entity> entities) {
        List<Entity> distinctEntities = new LinkedList<Entity>();
        Set<String> usedBatches = new HashSet<String>();
        for (Entity genealogyEntity : entities) {
            String batch = genealogyEntity.getStringField("batch");
            if (!usedBatches.contains(batch)) {
                usedBatches.add(batch);
                distinctEntities.add(genealogyEntity);
            }
        }
        return distinctEntities;
    }

    public void generateReportForProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponent batchState = (GridComponent) viewDefinitionState.getComponentByReference("batches");
        if (state instanceof FormComponent) {
            if ((batchState == null) || (batchState.getFieldValue() == null)) {
                state.addMessage("genealogies.genealogyForProduct.report.noBatch", MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/genealogies/genealogyForProduct.pdf?value=" + batchState.getFieldValue(), true,
                        false);
            }
        } else {
            state.addMessage("genealogies.genealogyForProduct.report.noBatch", MessageType.FAILURE);
        }
    }
}
