package com.qcadoo.mes.genealogiesForComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.genealogies.GenealogiesConstants;
import com.qcadoo.mes.genealogies.GenealogyService;
import com.qcadoo.mes.genealogies.print.ReportService;
import com.qcadoo.mes.orders.OrdersConstants;
import com.qcadoo.mes.technologies.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class GenealogiesForComponentsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private GenealogyService genealogyService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ReportService reportService;

    public void fillProductInComponents(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        ComponentState productGridLayout = state.getComponentByReference("productGridLayout");
        FieldComponent productInComponentsList = (FieldComponent) state.getComponentByReference("productInComponentsList");

        if (form.isValid()) {
            Entity genealogy = null;
            List<Entity> existingProductInComponents = Collections.emptyList();

            if (form.getEntityId() != null) {
                genealogy = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                        GenealogiesConstants.MODEL_GENEALOGY).get(form.getEntityId());
                existingProductInComponents = genealogy.getHasManyField("productInComponents");
            }

            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    Long.valueOf(form.getEntity().getField("order").toString()));
            Entity technology = order.getBelongsToField("technology");

            if (technology != null) {
                List<Entity> targetProductInComponents = new ArrayList<Entity>();

                List<Entity> operationComponents = new ArrayList<Entity>();
                genealogyService.addOperationsFromSubtechnologiesToList(technology.getTreeField("operationComponents"),
                        operationComponents);
                for (Entity operationComponent : operationComponents) {
                    for (Entity operationProductInComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                        if ((Boolean) operationProductInComponent.getField("batchRequired")) {
                            targetProductInComponents.add(createGenealogyProductInComponent(genealogy,
                                    operationProductInComponent, existingProductInComponents));
                        }
                    }
                }

                if (targetProductInComponents.isEmpty()) {
                    productGridLayout.setVisible(false);
                } else {
                    productInComponentsList.setFieldValue(targetProductInComponents);
                }
            } else {
                productGridLayout.setVisible(false);
            }
        }
    }

    private Entity createGenealogyProductInComponent(final Entity genealogy, final Entity operationProductInComponent,
            final List<Entity> existingProductInComponents) {
        for (Entity e : existingProductInComponents) {
            if (e.getBelongsToField("productInComponent").getId().equals(operationProductInComponent.getId())) {
                return e;
            }
        }
        Entity genealogyProductInComponent = dataDefinitionService.get(GenealogiesForComponentsConstants.PLUGIN_IDENTIFIER,
                GenealogiesForComponentsConstants.MODEL_GENEALOGY_PRODUCT_IN_COMPONENT).create();
        genealogyProductInComponent.setField("genealogy", genealogy);
        genealogyProductInComponent.setField("productInComponent", operationProductInComponent);
        genealogyProductInComponent.setField("batch", new ArrayList<Entity>());
        return genealogyProductInComponent;
    }

    public void disableBatchRequiredForTechnology(final ViewDefinitionState state) {
        ComponentState form = state.getComponentByReference("form");
        if (form.getFieldValue() != null) {
            FieldComponent batchRequired = (FieldComponent) state.getComponentByReference("batchRequired");
            if (checkProductInComponentsBatchRequired((Long) form.getFieldValue())) {
                batchRequired.setEnabled(false);
                batchRequired.setFieldValue("1");
                batchRequired.requestComponentUpdateState();
            } else {
                batchRequired.setEnabled(true);
            }
        }
    }

    public void fillBatchRequiredForTechnology(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField("batchRequired") != null && (Boolean) entity.getField("batchRequired")) {
            Entity technology = entity.getBelongsToField("operationComponent").getBelongsToField("technology");
            DataDefinition technologyInDef = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);
            Entity technologyEntity = technologyInDef.get(technology.getId());
            if (!(Boolean) technologyEntity.getField("batchRequired")) {
                technologyEntity.setField("batchRequired", true);
                technologyInDef.save(technologyEntity);
            }
        }
    }

    private boolean checkProductInComponentsBatchRequired(final Long entityId) {
        SearchResult searchResult = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).find()
                .addRestriction(Restrictions.eq("operationComponent.technology.id", entityId))
                .addRestriction(Restrictions.eq("batchRequired", true)).setMaxResults(1).list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

    public void generateReportForComponent(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            FieldComponent batchState = (FieldComponent) viewDefinitionState.getComponentByReference("batches");
            if (batchState != null && batchState.getFieldValue() != null) {
                viewDefinitionState.redirectTo(
                        "/genealogiesForComponents/genealogyForComponent.pdf?value=" + batchState.getFieldValue(), true, false);
            } else {
                state.addMessage(translationService.translate("genealogiesForComponents.genealogyForComponent.report.noBatch",
                        viewDefinitionState.getLocale()), MessageType.FAILURE);
            }
        } else {
            state.addMessage(translationService.translate("genealogiesForComponents.genealogyForComponent.report.noBatch",
                    viewDefinitionState.getLocale()), MessageType.FAILURE);
        }
    }

    public void addRestrictionToComponentGrid(final ViewDefinitionState viewDefinitionState) {
        final FieldComponent product = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        final GridComponent batches = (GridComponent) viewDefinitionState.getComponentByReference("batches");

        batches.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchCriteriaBuilder) {
                searchCriteriaBuilder.addRestriction(Restrictions.eq("productInComponent.productInComponent.product.id",
                        product.getFieldValue()));
            }

        });

        batches.setEntities(reportService.setDistinctBatch(batches.getEntities()));
    }
}
