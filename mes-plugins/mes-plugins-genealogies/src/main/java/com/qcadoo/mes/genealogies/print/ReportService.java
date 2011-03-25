/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.view.internal.ComponentState;
import com.qcadoo.view.internal.ViewDefinitionState;
import com.qcadoo.view.internal.ComponentState.MessageType;
import com.qcadoo.view.internal.components.form.FormComponentState;
import com.qcadoo.view.internal.components.grid.GridComponentState;
import com.qcadoo.view.internal.components.lookup.LookupComponentState;

@Service
public class ReportService {

    @Autowired
    private TranslationService translationService;

    public void generateReportForComponent(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponentState) {
            GridComponentState batchState = (GridComponentState) viewDefinitionState.getComponentByReference("batches");
            if (batchState != null && batchState.getFieldValue() != null) {
                viewDefinitionState.redirectTo("/genealogies/genealogyForComponent.pdf?value=" + batchState.getFieldValue(),
                        true, false);
            } else {
                state.addMessage(
                        translationService.translate("genealogies.genealogyForComponent.report.noBatch", state.getLocale()),
                        MessageType.FAILURE);
            }
        } else {
            state.addMessage(translationService.translate("genealogies.genealogyForComponent.report.noBatch", state.getLocale()),
                    MessageType.FAILURE);
        }
    }

    public void changeProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof LookupComponentState)) {
            return;
        }

        GridComponentState batches = (GridComponentState) viewDefinitionState.getComponentByReference("batches");

        batches.setSelectedEntityId(null);
        batches.setSelectedEntitiesId(new HashSet<Long>());
    }

    public void addRestrictionToGenealogyGrid(final ViewDefinitionState viewDefinitionState, final Locale locale) {
        final LookupComponentState product = (LookupComponentState) viewDefinitionState.getComponentByReference("product");
        final GridComponentState batches = (GridComponentState) viewDefinitionState.getComponentByReference("batches");

        batches.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchCriteriaBuilder) {
                searchCriteriaBuilder.restrictedWith(Restrictions.eq("order.product.id", product.getFieldValue()));
                searchCriteriaBuilder.withDistinctProperty("batch");
            }

        });
    }

    public void addRestrictionToComponentGrid(final ViewDefinitionState viewDefinitionState, final Locale locale) {
        final LookupComponentState product = (LookupComponentState) viewDefinitionState.getComponentByReference("product");
        final GridComponentState batches = (GridComponentState) viewDefinitionState.getComponentByReference("batches");

        batches.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchCriteriaBuilder) {
                searchCriteriaBuilder.restrictedWith(Restrictions.eq("productInComponent.productInComponent.product.id",
                        product.getFieldValue()));
                searchCriteriaBuilder.withDistinctProperty("batch");
            }

        });
    }

    public void generateReportForProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponentState batchState = (GridComponentState) viewDefinitionState.getComponentByReference("batches");
        if (state instanceof FormComponentState) {
            if (batchState != null && batchState.getFieldValue() != null) {
                viewDefinitionState.redirectTo("/genealogies/genealogyForProduct.pdf?value=" + batchState.getFieldValue(), true,
                        false);
            } else {
                state.addMessage(
                        translationService.translate("genealogies.genealogyForProduct.report.noBatch", state.getLocale()),
                        MessageType.FAILURE);
            }
        } else {
            state.addMessage(translationService.translate("genealogies.genealogyForProduct.report.noBatch", state.getLocale()),
                    MessageType.FAILURE);
        }
    }
}
