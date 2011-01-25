package com.qcadoo.mes.genealogies.print;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.search.CustomRestriction;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.components.lookup.LookupComponentState;

@Service
public class ReportService {

    @Autowired
    private TranslationService translationService;

    public void generateReportForComponent(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponentState batchState = (GridComponentState) viewDefinitionState.getComponentByReference("batches");
        if (state instanceof FormComponentState) {
            if (batchState != null && batchState.getFieldValue() != null) {
                viewDefinitionState
                        .redirectTo("/genealogies/genealogyForComponent.pdf?value=" + batchState.getFieldValue(), true);
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
    }

    public void addRestrictionToGenealogyGrid(final ViewDefinitionState viewDefinitionState, final Locale locale) {
        final LookupComponentState product = (LookupComponentState) viewDefinitionState.getComponentByReference("product");
        final GridComponentState batches = (GridComponentState) viewDefinitionState.getComponentByReference("batches");

        batches.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchCriteriaBuilder) {
                searchCriteriaBuilder.restrictedWith(Restrictions.eq("order.product.id", product.getFieldValue()));
                searchCriteriaBuilder.distinct();
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
                searchCriteriaBuilder.distinct();
            }

        });
    }

    public void generateReportForProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        GridComponentState batchState = (GridComponentState) viewDefinitionState.getComponentByReference("batches");
        if (state instanceof FormComponentState) {
            if (batchState != null && batchState.getFieldValue() != null) {
                viewDefinitionState.redirectTo("/genealogies/genealogyForProduct.pdf?value=" + batchState.getFieldValue(), true);
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
