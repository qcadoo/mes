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
import java.util.Collection;
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
import com.qcadoo.mes.beans.products.ProductsProduct;
import com.qcadoo.mes.beans.products.ProductsSubstitute;
import com.qcadoo.mes.beans.products.ProductsTechnology;
import com.qcadoo.mes.beans.products.ProductsWorkPlan;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.RestrictionOperator;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.internal.DateTimeType;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.products.print.pdf.MaterialRequirementPdfService;
import com.qcadoo.mes.products.print.pdf.WorkPlanForMachinePdfService;
import com.qcadoo.mes.products.print.pdf.WorkPlanForWorkerPdfService;
import com.qcadoo.mes.products.print.xls.MaterialRequirementXlsService;
import com.qcadoo.mes.products.print.xls.WorkPlanForMachineXlsService;
import com.qcadoo.mes.products.print.xls.WorkPlanForWorkerXlsService;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.lookup.LookupComponentState;

@Service
public final class ProductService {

    private static final SimpleDateFormat D_T_F = new SimpleDateFormat(DateType.REPORT_DATE_TIME_FORMAT);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialRequirementPdfService materialRequirementPdfService;

    @Autowired
    private MaterialRequirementXlsService materialRequirementXlsService;

    @Autowired
    private WorkPlanForWorkerPdfService workPlanForWorkerPdfService;

    @Autowired
    private WorkPlanForMachinePdfService workPlanForMachinePdfService;

    @Autowired
    private WorkPlanForWorkerXlsService workPlanForWorkerXlsService;

    @Autowired
    private WorkPlanForMachineXlsService workPlanForMachineXlsService;

    @Value("${reportPath}")
    private String path;

    @Autowired
    private TranslationService translationService;

    public boolean checkIfProductIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        ProductsProduct product = (ProductsProduct) entity.getField("product");

        if (product == null || product.getId() == null) {
            return true;
        }

        Entity productEntity = dataDefinitionService.get("products", "product").get(product.getId());

        if (productEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("product", null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfTechnologyIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        ProductsTechnology technology = (ProductsTechnology) entity.getField("technology");

        if (technology == null || technology.getId() == null) {
            return true;
        }

        Entity technologyEntity = dataDefinitionService.get("products", "technology").get(technology.getId());

        if (technologyEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("technology", null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfSubstituteIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        ProductsSubstitute substitute = (ProductsSubstitute) entity.getField("substitute");

        if (substitute == null || substitute.getId() == null) {
            return true;
        }

        Entity substituteEntity = dataDefinitionService.get("products", "substitute").get(substitute.getId());

        if (substituteEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("substitute", null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkSubstituteComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsProduct product = (ProductsProduct) entity.getField("product");
        ProductsSubstitute substitute = (ProductsSubstitute) entity.getField("substitute");

        if (substitute == null || product == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find()
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("product"), product.getId()))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("substitute"), substitute.getId())).list();

        if (searchResult.getTotalNumberOfEntities() == 1 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("product"), "products.validate.global.error.substituteComponentDuplicated");
            return false;
        } else {
            return true;
        }
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

    public void generateOrderNumber(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        FieldComponentState number = (FieldComponentState) state.getComponentByReference("number");

        if (form.getEntityId() != null) {
            // form is already saved
            return;
        }

        if (StringUtils.hasText((String) number.getFieldValue())) {
            // number is already choosen
            return;
        }

        if (number.isHasError()) {
            // there is a validation message for that field
            return;
        }

        SearchResult results = dataDefinitionService.get("products", "order").find().withMaxResults(1).orderDescBy("id").list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        String generatedNumber = String.format("%06d", longValue);

        number.setFieldValue(generatedNumber);
    }

    public void disableFormForDoneOrder(final ViewDefinitionState state, final Locale locale) {
        FormComponentState order = (FormComponentState) state.getComponentByReference("form");

        Entity entity = dataDefinitionService.get("products", "order").get(order.getEntityId());

        if (entity != null && "done".equals(entity.getStringField("state")) && order.isValid()) {
            order.setEnabled(false);
            setChildrenEnabled(order.getChildren().values(), false);
        }
    }

    private void setChildrenEnabled(final Collection<ComponentState> children, final boolean isEnabled) {
        for (ComponentState child : children) {
            child.setEnabled(isEnabled);
            if (child instanceof ContainerState) {
                setChildrenEnabled(((ContainerState) child).getChildren().values(), isEnabled);
            }
        }
    }

    public void changeOrderProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof LookupComponentState)) {
            return;
        }

        LookupComponentState product = (LookupComponentState) state;
        LookupComponentState technology = (LookupComponentState) viewDefinitionState.getComponentByReference("technology");
        FieldComponentState defaultTechnology = (FieldComponentState) viewDefinitionState
                .getComponentByReference("defaultTechnology");

        defaultTechnology.setFieldValue("");
        technology.setFieldValue(null);

        if (product.getFieldValue() != null) {
            Entity defaultTechnologyEntity = getDefaultTechnology(product.getFieldValue());

            if (defaultTechnologyEntity != null) {
                technology.setFieldValue(defaultTechnologyEntity.getId());
            }
        }
    }

    public void fillDefaultTechnology(final ViewDefinitionState state, final Locale locale) {
        LookupComponentState product = (LookupComponentState) state.getComponentByReference("window.order.product");
        FieldComponentState defaultTechnology = (FieldComponentState) state.getComponentByReference("defaultTechnology");

        if (product.getFieldValue() != null) {
            Entity defaultTechnologyEntity = getDefaultTechnology(product.getFieldValue());

            if (defaultTechnologyEntity != null) {
                String defaultTechnologyValue = ExpressionUtil.getValue(defaultTechnologyEntity, "#name + ' - ' + #number",
                        locale);
                defaultTechnology.setFieldValue(defaultTechnologyValue);
            }
        }
    }

    public void disableTechnologiesIfProductDoesNotAny(final ViewDefinitionState state, final Locale locale) {
        LookupComponentState product = (LookupComponentState) state.getComponentByReference("window.order.product");
        LookupComponentState technology = (LookupComponentState) state.getComponentByReference("technology");
        FieldComponentState plannedQuantity = (FieldComponentState) state.getComponentByReference("plannedQuantity");

        if (product.getFieldValue() == null || !hasAnyTechnologies(product.getFieldValue())) {
            technology.setEnabled(false);
            technology.setRequired(false);
            plannedQuantity.setEnabled(false);
            plannedQuantity.setRequired(false);
        } else {
            technology.setEnabled(true);
            technology.setRequired(true);
            plannedQuantity.setEnabled(true);
            plannedQuantity.setRequired(true);
        }
    }

    private Entity getDefaultTechnology(final Long selectedProductId) {
        DataDefinition instructionDD = dataDefinitionService.get("products", "technology");

        SearchCriteriaBuilder searchCriteria = instructionDD.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(instructionDD.getField("master"), true))
                .restrictedWith(Restrictions.belongsTo(instructionDD.getField("product"), selectedProductId));

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

    private boolean hasAnyTechnologies(final Long selectedProductId) {
        DataDefinition technologyDD = dataDefinitionService.get("products", "technology");

        SearchCriteriaBuilder searchCriteria = technologyDD.find().withMaxResults(1)
                .restrictedWith(Restrictions.belongsTo(technologyDD.getField("product"), selectedProductId));

        SearchResult searchResult = searchCriteria.list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

    public boolean checkIfStateChangeIsCorrect(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder searchCriteria = dataDefinition.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(dataDefinition.getField("state"), "inProgress"))
                .restrictedWith(Restrictions.idRestriction(entity.getId(), RestrictionOperator.EQ));

        SearchResult searchResult = searchCriteria.list();

        if (entity.getField("state").toString().equals("pending") && searchResult.getTotalNumberOfEntities() > 0) {
            entity.addError(dataDefinition.getField("state"), "products.validate.global.error.illegalStateChange");
            return false;
        }
        return true;
    }

    public boolean checkTechnologyDefault(final DataDefinition dataDefinition, final Entity entity) {
        Boolean master = (Boolean) entity.getField("master");

        if (!master) {
            return true;
        }

        SearchCriteriaBuilder searchCriteria = dataDefinition.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(dataDefinition.getField("master"), true))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("product"), entity.getField("product")));

        if (entity.getId() != null) {
            searchCriteria.restrictedWith(Restrictions.idRestriction(entity.getId(), RestrictionOperator.NE));
        }

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 0) {
            return true;
        } else {
            entity.addError(dataDefinition.getField("master"), "products.validate.global.error.default");
            return false;
        }
    }

    public boolean checkOrderPlannedQuantity(final DataDefinition dataDefinition, final Entity entity) {
        ProductsProduct product = (ProductsProduct) entity.getField("product");
        if (product == null) {
            return true;
        }
        Object o = entity.getField("plannedQuantity");
        if (o == null) {
            entity.addError(dataDefinition.getField("plannedQuantity"), "products.validate.global.error.illegalStateChange");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkOrderTechnology(final DataDefinition dataDefinition, final Entity entity) {
        ProductsProduct product = (ProductsProduct) entity.getField("product");
        if (product == null) {
            return true;
        }
        if (entity.getField("technology") == null) {
            if (hasAnyTechnologies(product.getId())) {
                entity.addError(dataDefinition.getField("technology"), "products.validate.global.error.technologyError");
                return false;
            }
        }
        return true;
    }

    public boolean checkOrderDates(final DataDefinition dataDefinition, final Entity entity) {
        return compareDates(dataDefinition, entity, "dateFrom", "dateTo");
    }

    private String getLoginOfLoggedUser() {
        UsersUser user = securityService.getCurrentUser();
        return user.getUserName();
    }

    public void fillOrderDatesAndWorkers(final DataDefinition dataDefinition, final Entity entity) {
        if (("pending".equals(entity.getField("state")) || "done".equals(entity.getField("state")))
                && entity.getField("effectiveDateFrom") == null) {
            entity.setField("effectiveDateFrom", new Date());
            entity.setField("startWorker", getLoginOfLoggedUser());
        }
        if ("done".equals(entity.getField("state")) && entity.getField("effectiveDateTo") == null) {
            entity.setField("effectiveDateTo", new Date());
            entity.setField("endWorker", getLoginOfLoggedUser());

        }

        if (!entity.getField("state").toString().equals("inProgress")) {
            if (entity.getField("effectiveDateTo") != null) {
                entity.setField("state", "done");
            } else if (entity.getField("effectiveDateFrom") != null) {
                entity.setField("state", "pending");
            }
        }
    }

    private boolean compareDates(final DataDefinition dataDefinition, final Entity entity, final String dateFromField,
            final String dateToField) {
        Date dateFrom = (Date) entity.getField(dateFromField);
        Date dateTo = (Date) entity.getField(dateToField);

        if (dateFrom == null || dateTo == null) {
            return true;
        }

        if (dateFrom.after(dateTo)) {
            entity.addError(dataDefinition.getField(dateToField), "products.validate.global.error.datesOrder");
            return false;
        } else {
            return true;
        }
    }

    public void generateMaterialRequirement(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponentState) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(getLoginOfLoggedUser());
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

            Entity materialRequirement = dataDefinitionService.get("products", "materialRequirement").get(
                    (Long) state.getFieldValue());

            if (materialRequirement == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (StringUtils.hasText(materialRequirement.getStringField("fileName"))) {
                String message = translationService.translate(
                        "products.materialRequirement.window.materialRequirement.documentsWasGenerated", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
            } else {
                try {

                    materialRequirement = updateFileName(materialRequirement,
                            getFullFileName((Date) materialRequirement.getField("date"), "MaterialRequirement"),
                            "materialRequirement");
                    materialRequirementPdfService.generateDocument(materialRequirement, state.getLocale());
                    materialRequirementXlsService.generateDocument(materialRequirement, state.getLocale());
                    state.performEvent(viewDefinitionState, "reset", new String[0]);
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                } catch (DocumentException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        }
    }

    public void printMaterialRequirement(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        if (state.getFieldValue() != null && state.getFieldValue() instanceof Long) {
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

    public void printOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state.getFieldValue() != null && state.getFieldValue() instanceof Long) {
            viewDefinitionState.redirectTo("/products/order." + args[0] + "?id=" + state.getFieldValue(), false);
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

    public void disableFormForExistingWorkPlan(final ViewDefinitionState state, final Locale locale) {
        ComponentState name = state.getComponentByReference("name");
        ComponentState workPlanComponents = state.getComponentByReference("workPlanComponents");
        FieldComponentState generated = (FieldComponentState) state.getComponentByReference("generated");

        if ("1".equals(generated.getFieldValue())) {
            name.setEnabled(false);
            workPlanComponents.setEnabled(false);
        }
    }

    public void generateWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state instanceof FormComponentState) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(getLoginOfLoggedUser());
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

            Entity workPlan = dataDefinitionService.get("products", "workPlan").get((Long) state.getFieldValue());

            if (workPlan == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (StringUtils.hasText(workPlan.getStringField("fileName"))) {
                String message = translationService.translate("products.workPlan.window.workPlan.documentsWasGenerated",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
            } else {
                try {

                    workPlan = updateFileName(workPlan, getFullFileName((Date) workPlan.getField("date"), "WorkPlan"), "workPlan");
                    workPlanForMachinePdfService.generateDocument(workPlan, state.getLocale());
                    workPlanForMachineXlsService.generateDocument(workPlan, state.getLocale());
                    workPlanForWorkerPdfService.generateDocument(workPlan, state.getLocale());
                    workPlanForWorkerXlsService.generateDocument(workPlan, state.getLocale());
                    state.performEvent(viewDefinitionState, "reset", new String[0]);
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                } catch (DocumentException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        }
    }

    public void printWorkPlan(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

        if (state.getFieldValue() != null && state.getFieldValue() instanceof Long) {
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

    private final String getFullFileName(final Date date, final String fileName) {
        return path + fileName + "_" + D_T_F.format(date);
    }

    private final Entity updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        return dataDefinitionService.get("products", entityName).save(entity);
    }

}
