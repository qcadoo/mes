/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.workPlans.print;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.technologies.grouping.OperationMergeService;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.pdf.document.WorkPlanPdf;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.factory.GroupingContainerFactory;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.report.api.pdf.PdfDocumentWithWriterService;

@Service
public class WorkPlanPdfService extends PdfDocumentWithWriterService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private GroupingContainerFactory groupingContainerFactory;

    @Autowired
    private WorkPlanPdf workPlanPdf;

    @Autowired
    private OperationMergeService operationMergeService;

    @Autowired
    private ProductQuantitiesServiceImpl productQuantitiesServiceImpl;

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("workPlans.workPlan.report.title", locale);
    }

    @Override
    public void buildPdfContent(final PdfWriter writer, final Document document, final Entity workPlan, final Locale locale)
            throws DocumentException {

        GroupingContainer groupingContainer = groupingContainerFactory.create(workPlan, locale);

        for (Entity order : orders(workPlan)) {
            OperationProductComponentWithQuantityContainer productQuantities = productQuantitiesServiceImpl
                    .getProductComponentQuantities(order);
            removeAlreadyExistsMergesForOrder(order);
            for (Entity operationComponent : operationComponents(technology(order))) {
                Entity updatedComponent = updateOperationProductComponents(order, operationComponent);
                groupingContainer.add(order, updatedComponent, productQuantities);
            }
        }

        workPlanPdf.print(writer, groupingContainer, workPlan, document, locale);

    }

    public Entity updateOperationProductComponents(final Entity order, final Entity operationComponent) {
        if (!OrderState.PENDING.getStringValue().equals(order.getStringField(OrderFields.STATE))) {
            List<Entity> productionCountingQuantities = basicProductionCountingService
                    .getMaterialsForOperationFromProductionCountingQuantities(order, operationComponent);

            List<Entity> inComponents = updateOperationProductComponents(operationComponent, productionCountingQuantities,
                    TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS,
                    ProductionCountingQuantityRole.USED.getStringValue(), dataDefinitionService.get(
                            TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT));

            List<Entity> outComponents = updateOperationProductComponents(operationComponent, productionCountingQuantities,
                    TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS,
                    ProductionCountingQuantityRole.PRODUCED.getStringValue(), dataDefinitionService.get(
                            TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT));

            operationComponent.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS, inComponents);
            operationComponent.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS, outComponents);

        }
        return operationComponent;
    }

    private List<Entity> updateOperationProductComponents(final Entity operationComponent,
            final List<Entity> productionCountingQuantities, final String componentsField, final String role,
            final DataDefinition dataDefinition) {

        List<Entity> productionCountingQuantitiesForRole = productionCountingQuantities.stream()
                .filter(pcq -> role.equals(pcq.getStringField(ProductionCountingQuantityFields.ROLE)))
                .collect(Collectors.toList());
        List<Long> allProductionCountingQuantityIds = productionCountingQuantitiesForRole.stream()
                .map(pcq -> pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId()).collect(Collectors.toList());

        List<Entity> existingProductComponents = operationComponent
                .getHasManyField(componentsField)
                .stream()
                .filter(opic -> allProductionCountingQuantityIds.contains(opic.getBelongsToField(
                        OperationProductInComponentFields.PRODUCT).getId())).collect(Collectors.toList());

        List<Long> existingProductComponentIds = existingProductComponents.stream()
                .map(opic -> opic.getBelongsToField(OperationProductInComponentFields.PRODUCT).getId())
                .collect(Collectors.toList());

        List<Entity> mergedProductComponents = Lists.newArrayList();
        for (Entity pcq : productionCountingQuantitiesForRole) {
            Entity product = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            BigDecimal plannedQuantity = pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
            if (existingProductComponentIds.contains(product.getId())) {
                Optional<Entity> maybeExistingComponent = existingProductComponents
                        .stream()
                        .filter(component -> component.getBelongsToField(OperationProductInComponentFields.PRODUCT).getId()
                                .equals(product.getId())).findFirst();
                if (maybeExistingComponent.isPresent()) {
                    Entity existingComponent = maybeExistingComponent.get();
                    existingComponent.setField(OperationProductInComponentFields.QUANTITY, plannedQuantity);
                    mergedProductComponents.add(existingComponent);
                }
            } else {
                Entity newComponent = dataDefinition.create();
                newComponent.setField(OperationProductInComponentFields.PRODUCT, product);
                newComponent.setField(OperationProductInComponentFields.QUANTITY, plannedQuantity);
                newComponent.setField(OperationProductInComponentFields.OPERATION_COMPONENT, operationComponent);
                mergedProductComponents.add(newComponent);
            }
        }
        return mergedProductComponents;
    }

    private List<Entity> operationComponents(Entity technology) {
        return entityTreeUtilsService.getSortedEntities(technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS));
    }

    private Entity technology(Entity order) {
        return order.getBelongsToField(OrderFields.TECHNOLOGY);
    }

    private EntityList orders(Entity workPlan) {
        return workPlan.getHasManyField(WorkPlanFields.ORDERS);
    }

    private void removeAlreadyExistsMergesForOrder(Entity order) {
        List<Entity> mergedProductInsByOrder = operationMergeService.findMergedProductInByOrder(order);
        for (Entity entity : mergedProductInsByOrder) {
            entity.getDataDefinition().delete(entity.getId());
        }

        List<Entity> mergedProductOutsByOrder = operationMergeService.findMergedProductOutByOrder(order);
        for (Entity entity : mergedProductOutsByOrder) {
            entity.getDataDefinition().delete(entity.getId());
        }
    }

}
