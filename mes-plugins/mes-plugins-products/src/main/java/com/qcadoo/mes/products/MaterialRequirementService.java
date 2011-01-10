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
import com.qcadoo.mes.beans.products.ProductsMaterialRequirement;
import com.qcadoo.mes.beans.products.ProductsOrder;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.internal.DateTimeType;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.products.print.pdf.MaterialRequirementPdfService;
import com.qcadoo.mes.products.print.xls.MaterialRequirementXlsService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public final class MaterialRequirementService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialRequirementPdfService materialRequirementPdfService;

    @Autowired
    private MaterialRequirementXlsService materialRequirementXlsService;

    @Value("${reportPath}")
    private String path;

    @Autowired
    private TranslationService translationService;

    public void clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("date", null);
        entity.setField("worker", null);
    }

    public boolean checkMaterialRequirementComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsOrder order = (ProductsOrder) entity.getField("order");
        ProductsMaterialRequirement materialRequirement = (ProductsMaterialRequirement) entity.getField("materialRequirement");

        if (materialRequirement == null || order == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition
                .find()
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("order"), order.getId()))
                .restrictedWith(
                        Restrictions.belongsTo(dataDefinition.getField("materialRequirement"), materialRequirement.getId()))
                .list();

        if (searchResult.getTotalNumberOfEntities() == 1 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("order"), "products.validate.global.error.materialRequirementDuplicated");
            return false;
        } else {
            return true;
        }
    }

    public void disableFormForExistingMaterialRequirement(final ViewDefinitionState state, final Locale locale) {
        ComponentState name = state.getComponentByReference("name");
        ComponentState onlyComponents = state.getComponentByReference("onlyComponents");
        ComponentState materialRequirementComponents = state.getComponentByReference("materialRequirementComponents");
        FieldComponentState generated = (FieldComponentState) state.getComponentByReference("generated");

        if ("1".equals(generated.getFieldValue())) {
            name.setEnabled(false);
            onlyComponents.setEnabled(false);
            materialRequirementComponents.setEnabled(false);
        }
    }

    public void generateMaterialRequirement(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponentState) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity materialRequirement = dataDefinitionService.get("products", "materialRequirement").get(
                    (Long) state.getFieldValue());

            if (materialRequirement == null) {
                String message = translationService.translate("core.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(materialRequirement.getStringField("fileName"))) {
                String message = translationService.translate(
                        "products.materialRequirement.window.materialRequirement.documentsWasGenerated", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (materialRequirement.getHasManyField("orders").isEmpty()) {
                String message = translationService.translate(
                        "products.materialRequirement.window.materialRequirement.missingAssosiatedOrders", state.getLocale());
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

            materialRequirement = dataDefinitionService.get("products", "materialRequirement").get((Long) state.getFieldValue());

            try {
                generateMaterialReqDocuments(state, materialRequirement);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public void printMaterialRequirement(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        if (state.getFieldValue() instanceof Long) {
            Entity materialRequirement = dataDefinitionService.get("products", "materialRequirement").get(
                    (Long) state.getFieldValue());
            if (materialRequirement == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(materialRequirement.getStringField("fileName"))) {
                state.addMessage(translationService.translate(
                        "products.materialRequirement.window.materialRequirement.documentsWasNotGenerated", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                viewDefinitionState
                        .redirectTo("/products/materialRequirement." + args[0] + "?id=" + state.getFieldValue(), false);
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

    public void printMaterialReqForOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
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
            } else {
                Entity materialRequirement = createNewMaterialReq(order, state);
                try {
                    generateMaterialReqDocuments(state, materialRequirement);

                    viewDefinitionState.redirectTo(
                            "/products/materialRequirement." + args[0] + "?id=" + materialRequirement.getId(), false);
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

    private String generateMaterialReqNumber() {
        SearchResult results = dataDefinitionService.get("products", "materialRequirement").find().withMaxResults(1)
                .orderDescBy("id").list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        String generatedNumber = String.format("%06d", longValue);

        return generatedNumber;
    }

    private void generateMaterialReqDocuments(final ComponentState state, Entity materialRequirement) throws IOException,
            DocumentException {
        materialRequirement = updateFileName(materialRequirement,
                getFullFileName((Date) materialRequirement.getField("date"), "Material_requirement"), "materialRequirement");
        materialRequirementPdfService.generateDocument(materialRequirement, state.getLocale());
        materialRequirementXlsService.generateDocument(materialRequirement, state.getLocale());
    }

    private Entity createNewMaterialReq(final Entity order, final ComponentState state) {

        Entity materialReq = new DefaultEntity("products", "materialRequirement");
        materialReq.setField(
                "name",
                generateMaterialReqNumber() + " "
                        + translationService.translate("products.materialReq.forOrder", state.getLocale()) + " "
                        + order.getField("number"));
        materialReq.setField("generated", true);
        materialReq.setField("worker", securityService.getCurrentUserName());
        materialReq.setField("date", new Date());
        materialReq.setField("onlyComponents", false);

        DataDefinition data = dataDefinitionService.get("products", "materialRequirement");
        Entity saved = data.save(materialReq);

        Entity materialReqComponent = new DefaultEntity("products", "materialRequirementComponent");
        materialReqComponent.setField("order", order);
        materialReqComponent.setField("materialRequirement", saved);

        DataDefinition workPlanComponentDef = dataDefinitionService.get("products", "materialRequirementComponent");
        workPlanComponentDef.save(materialReqComponent);

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
