/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.products;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.products.ProductsOrder;
import com.qcadoo.mes.beans.products.ProductsWorkPlan;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.internal.DateTimeType;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.products.print.pdf.WorkPlanForMachinePdfService;
import com.qcadoo.mes.products.print.pdf.WorkPlanForProductPdfService;
import com.qcadoo.mes.products.print.pdf.WorkPlanForWorkerPdfService;
import com.qcadoo.mes.products.print.xls.WorkPlanForMachineXlsService;
import com.qcadoo.mes.products.print.xls.WorkPlanForProductXlsService;
import com.qcadoo.mes.products.print.xls.WorkPlanForWorkerXlsService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public final class WorkPlanService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private WorkPlanForWorkerPdfService workPlanForWorkerPdfService;

    @Autowired
    private WorkPlanForMachinePdfService workPlanForMachinePdfService;

    @Autowired
    private WorkPlanForProductPdfService workPlanForProductPdfService;

    @Autowired
    private WorkPlanForWorkerXlsService workPlanForWorkerXlsService;

    @Autowired
    private WorkPlanForMachineXlsService workPlanForMachineXlsService;

    @Autowired
    private WorkPlanForProductXlsService workPlanForProductXlsService;

    @Value("${reportPath}")
    private String path;

    @Autowired
    private TranslationService translationService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("date", null);
        entity.setField("worker", null);
        return true;
    }

    public void generateWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state instanceof FormComponentState) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity workPlan = dataDefinitionService.get("products", "workPlan").get((Long) state.getFieldValue());

            if (workPlan == null) {
                String message = translationService.translate("core.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(workPlan.getStringField("fileName"))) {
                String message = translationService.translate("products.workPlan.window.workPlan.documentsWasGenerated",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (workPlan.getHasManyField("orders").isEmpty()) {
                String message = translationService.translate("products.workPlan.window.workPlan.missingAssosiatedOrders",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateTimeType.DATE_TIME_FORMAT).format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponentState) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            workPlan = dataDefinitionService.get("products", "workPlan").get((Long) state.getFieldValue());

            try {
                generateWorkPlanDocuments(state, workPlan);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public void printWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

        if (state.getFieldValue() instanceof Long) {
            Entity workPlan = dataDefinitionService.get("products", "workPlan").get((Long) state.getFieldValue());
            if (workPlan == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(workPlan.getStringField("fileName"))) {
                state.addMessage(
                        translationService.translate("products.workPlan.window.workPlan.documentsWasNotGenerated",
                                state.getLocale()), MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/products/workPlan" + args[1] + "." + args[0] + "?id=" + state.getFieldValue(),
                        false);
            }
        } else {
            if (state instanceof FormComponentState) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void disableFormForExistingWorkPlan(final ViewDefinitionState state, final Locale locale) {
        ComponentState name = state.getComponentByReference("name");
        ComponentState workPlanComponents = state.getComponentByReference("workPlanComponents");
        FieldComponentState generated = (FieldComponentState) state.getComponentByReference("generated");

        if ("1".equals(generated.getFieldValue())) {
            name.setEnabled(false);
            workPlanComponents.setEnabled(false);
        } else {
            name.setEnabled(true);
        }
    }

    public boolean checkWorkPlanComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsOrder order = (ProductsOrder) entity.getField("order");
        ProductsWorkPlan workPlan = (ProductsWorkPlan) entity.getField("workPlan");

        if (workPlan == null || order == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find()
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("order"), order.getId()))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("workPlan"), workPlan.getId())).list();

        if (searchResult.getTotalNumberOfEntities() == 1 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("order"), "products.validate.global.error.workPlanDuplicated");
            return false;
        } else {
            return true;
        }
    }

    public void printWorkPlanForOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get("products", "order").get((Long) state.getFieldValue());

            if (order == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (order.getField("technology") == null) {
                state.addMessage(
                        translationService.translate("products.validate.global.error.orderMustHaveTechnology", state.getLocale()),
                        MessageType.FAILURE);
            } else if (order.getBelongsToField("technology").getHasManyField("operationComponents").isEmpty()) {
                state.addMessage(
                        translationService.translate("products.validate.global.error.orderTechnologyMustHaveOperation",
                                state.getLocale()), MessageType.FAILURE);
            } else {
                Entity workPlan = createNewWorkPlan(order, state);
                try {
                    generateWorkPlanDocuments(state, workPlan);

                    viewDefinitionState.redirectTo("/products/workPlan" + args[1] + "." + args[0] + "?id=" + workPlan.getId(),
                            false);
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                } catch (DocumentException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        } else {
            if (state instanceof FormComponentState) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }

    }

    private String generateWorkPlanNumber() {
        SearchResult results = dataDefinitionService.get("products", "workPlan").find().withMaxResults(1).orderDescBy("id")
                .list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        String generatedNumber = String.format("%06d", longValue);

        return generatedNumber;
    }

    private void generateWorkPlanDocuments(final ComponentState state, Entity workPlan) throws IOException, DocumentException {
        workPlan = updateFileName(workPlan, getFullFileName((Date) workPlan.getField("date"), "Work_plan"), "workPlan");
        workPlanForMachinePdfService.generateDocument(workPlan, state.getLocale());
        workPlanForMachineXlsService.generateDocument(workPlan, state.getLocale());
        workPlanForWorkerPdfService.generateDocument(workPlan, state.getLocale());
        workPlanForWorkerXlsService.generateDocument(workPlan, state.getLocale());
        workPlanForProductPdfService.generateDocument(workPlan, state.getLocale());
        workPlanForProductXlsService.generateDocument(workPlan, state.getLocale());
    }

    private Entity createNewWorkPlan(final Entity order, final ComponentState state) {

        Entity workPlan = new DefaultEntity("products", "workPlan");
        workPlan.setField("name",
                generateWorkPlanNumber() + " " + translationService.translate("products.workPlan.forOrder", state.getLocale())
                        + " " + order.getField("number"));
        workPlan.setField("generated", true);
        workPlan.setField("worker", securityService.getCurrentUserName());
        workPlan.setField("date", new Date());

        DataDefinition data = dataDefinitionService.get("products", "workPlan");
        Entity saved = data.save(workPlan);

        Entity workPlanComponent = new DefaultEntity("products", "workPlanComponent");
        workPlanComponent.setField("order", order);
        workPlanComponent.setField("workPlan", saved);

        DataDefinition workPlanComponentDef = dataDefinitionService.get("products", "workPlanComponent");
        workPlanComponentDef.save(workPlanComponent);

        return saved;
    }

    private final String getFullFileName(final Date date, final String fileName) {
        return path + fileName + "_" + new SimpleDateFormat(DateType.REPORT_DATE_TIME_FORMAT).format(date) + "_";
    }

    private final Entity updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        return dataDefinitionService.get("products", entityName).save(entity);
    }

}
