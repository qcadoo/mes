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
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
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
import com.qcadoo.mes.products.print.pdf.MaterialRequirementPdfService;
import com.qcadoo.mes.products.print.pdf.WorkPlanPdfService;
import com.qcadoo.mes.products.print.xls.MaterialRequirementXlsService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;

@Service
public final class ProductService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialRequirementPdfService materialRequirementPdfService;

    @Autowired
    private MaterialRequirementXlsService materialRequirementXlsService;

    @Autowired
    private WorkPlanPdfService workPlanPdfService;

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

    // @SuppressWarnings("unchecked")
    // public void disableFormForExistingWorkPlan(final ViewValue<Long> value, final String triggerComponentName,
    // final Entity entity, final Locale locale) throws IOException, DocumentException {
    // if (value.lookupValue("mainWindow.workPlanDetailsForm") == null
    // || value.lookupValue("mainWindow.workPlanDetailsForm").getValue() == null
    // || ((FormValue) value.lookupValue("mainWindow.workPlanDetailsForm").getValue()).getId() == null) {
    //
    // return;
    // }
    //
    // String generatedStringValue = ((SimpleValue) value.lookupValue("mainWindow.workPlanDetailsForm.generated").getValue())
    // .getValue().toString();
    //
    // boolean isGenerated = true;
    // if ("0".equals(generatedStringValue)) {
    // isGenerated = false;
    // }
    //
    // if (isGenerated) {
    // value.lookupValue("mainWindow.workPlanDetailsForm.name").setEnabled(false);
    // value.lookupValue("mainWindow.ordersGrid").setEnabled(false);
    //
    // if ("mainWindow.workPlanDetailsForm".equals(triggerComponentName)) {
    // Entity workPlan = dataDefinitionService.get("products", "workPlan").get(
    // ((FormValue) value.lookupValue("mainWindow.workPlanDetailsForm").getValue()).getId());
    //
    // if (workPlan.getField("fileName") == null || "".equals(workPlan.getField("fileName").toString().trim())) {
    // workPlanPdfService.generateDocument(workPlan, locale);
    // // workPlanXlsService.generateDocument(workPlan, locale);
    // } else {
    // // FIXME KRNA remove override
    // value.addInfoMessage("override:"
    // + translationService.translate(
    // "products.workPlanDetailsView.mainWindow.workPlanDetailsForm.documentsWasGenerated", locale));
    // }
    //
    // }
    // }
    // }

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
        FormComponentState form = (FormComponentState) state.getComponentByPath("window.order");
        FieldComponentState number = (FieldComponentState) state.getComponentByPath("window.order.number");

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

    // @SuppressWarnings("unchecked")
    // public void afterOrderDetailsLoad(final ViewValue<Long> value, final String triggerComponentName, final Entity entity,
    // final Locale locale) {
    // generateOrderNumber(value, triggerComponentName, locale);
    //
    // ViewValue<LookupData> productValue = (ViewValue<LookupData>) value.lookupValue("mainWindow.orderDetailsForm.product");
    // ViewValue<SimpleValue> defaultTechnologyValue = (ViewValue<SimpleValue>) value
    // .lookupValue("mainWindow.orderDetailsForm.defaultTechnology");
    // ViewValue<LookupData> technologyValue = (ViewValue<LookupData>) value
    // .lookupValue("mainWindow.orderDetailsForm.technology");
    // ViewValue<SimpleValue> stateValue = (ViewValue<SimpleValue>) value.lookupValue("mainWindow.orderDetailsForm.state");
    // ViewValue<FormValue> formValue = (ViewValue<FormValue>) value.lookupValue("mainWindow.orderDetailsForm");
    // ViewValue<SimpleValue> plannedQuantityValue = (ViewValue<SimpleValue>) value
    // .lookupValue("mainWindow.orderDetailsForm.plannedQuantity");
    //
    // if (stateValue != null && stateValue.getValue() != null && stateValue.getValue().getValue() != null
    // && stateValue.getValue().getValue().equals("done") && entity.isValid()) {
    // formValue.setEnabled(false);
    // }
    //
    // if (formValue == null) {
    // return;
    // }
    //
    // if (defaultTechnologyValue == null) {
    // defaultTechnologyValue = new ViewValue<SimpleValue>(new SimpleValue(""));
    // defaultTechnologyValue.setVisible(true);
    // formValue.addComponent("defaultTechnology", defaultTechnologyValue);
    // }
    // if (plannedQuantityValue == null) {
    // plannedQuantityValue = new ViewValue<SimpleValue>(new SimpleValue(null));
    // plannedQuantityValue.setVisible(true);
    // plannedQuantityValue.setEnabled(true);
    // formValue.addComponent("plannedQuantity", plannedQuantityValue);
    // }
    //
    // defaultTechnologyValue.setEnabled(false);
    // defaultTechnologyValue.setValue(new SimpleValue(""));
    // technologyValue.setEnabled(true);
    //
    // Long selectedProductId = null;
    //
    // if (productValue.getValue() != null && productValue.getValue().getSelectedEntityId() != null) {
    // selectedProductId = productValue.getValue().getSelectedEntityId();
    // }
    //
    // Entity selectedTechnology = null;
    //
    // if (selectedProductId != null && technologyValue.getValue() != null
    // && technologyValue.getValue().getSelectedEntityId() != null
    // && !"mainWindow.orderDetailsForm.product".equals(triggerComponentName)) {
    // selectedTechnology = dataDefinitionService.get("products", "technology").get(
    // technologyValue.getValue().getSelectedEntityId());
    // } else {
    // technologyValue.getValue().setSelectedEntityId(null);
    // technologyValue.getValue().setSelectedEntityCode("");
    // technologyValue.getValue().setSelectedEntityValue("");
    // }
    //
    // if (selectedProductId == null) {
    // technologyValue.setEnabled(false);
    // technologyValue.getValue().setSelectedEntityId(null);
    // technologyValue.getValue().setSelectedEntityCode("");
    // technologyValue.getValue().setSelectedEntityValue("");
    // technologyValue.getValue().setRequired(false);
    // plannedQuantityValue.getValue().setRequired(false);
    // } else {
    // plannedQuantityValue.getValue().setRequired(true);
    // if (!hasAnyTechnologies(selectedProductId)) {
    // technologyValue.setEnabled(false);
    // technologyValue.getValue().setRequired(false);
    // technologyValue.getValue().setSelectedEntityId(null);
    // technologyValue.getValue().setSelectedEntityCode("");
    // technologyValue.getValue().setSelectedEntityValue("");
    // } else {
    // technologyValue.getValue().setRequired(true);
    // Entity defaultTechnologyEntity = getDefaultTechnology(selectedProductId);
    // if (defaultTechnologyEntity != null) {
    // String defaultTechnologyName = defaultTechnologyEntity.getField("name").toString();
    // defaultTechnologyValue.getValue().setValue(defaultTechnologyName);
    // if (selectedTechnology == null && "mainWindow.orderDetailsForm.product".equals(triggerComponentName)) {
    // selectDefaultTechnology(technologyValue, defaultTechnologyEntity);
    // }
    // }
    // }
    // }
    // }

    // private void selectDefaultTechnology(final ViewValue<LookupData> technologyValue, final Entity defaultTechnologyEntity) {
    // ViewDefinition viewDefinition = viewDefinitionService.get("products", "orderDetailsView");
    // LookupComponent lookupTechnology = (LookupComponent) viewDefinition
    // .lookupComponent("mainWindow.orderDetailsForm.technology");
    // technologyValue.getValue().setValue(defaultTechnologyEntity.getId());
    // technologyValue.getValue().setSelectedEntityCode(defaultTechnologyEntity.getStringField(lookupTechnology.getFieldCode()));
    // technologyValue.getValue().setSelectedEntityValue(
    // ExpressionUtil.getValue(defaultTechnologyEntity, lookupTechnology.getExpression()));
    // }

    private Entity getDefaultTechnology(final Long selectedProductId) {
        DataDefinition technologyDD = dataDefinitionService.get("products", "technology");

        SearchCriteriaBuilder searchCriteria = technologyDD.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(technologyDD.getField("master"), true))
                .restrictedWith(Restrictions.belongsTo(technologyDD.getField("product"), selectedProductId));

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

    public void selectDefaultInstruction(final ViewDefinitionState state, final Locale locale) {
        // ViewValue<LookupData> productValue = (ViewValue<LookupData>) value.lookupValue("mainWindow.orderDetailsForm.product");
        // ViewValue<SimpleValue> defaultInstructionValue = (ViewValue<SimpleValue>) value
        // .lookupValue("mainWindow.orderDetailsForm.defaultInstruction");
        // ViewValue<LookupData> instructionValue = (ViewValue<LookupData>) value
        // .lookupValue("mainWindow.orderDetailsForm.instruction");
        // ViewValue<SimpleValue> stateValue = (ViewValue<SimpleValue>) value.lookupValue("mainWindow.orderDetailsForm.state");
        // ViewValue<FormValue> formValue = (ViewValue<FormValue>) value.lookupValue("mainWindow.orderDetailsForm");
        // ViewValue<SimpleValue> plannedQuantityValue = (ViewValue<SimpleValue>) value
        // .lookupValue("mainWindow.orderDetailsForm.plannedQuantity");
        //
        // if (stateValue != null && stateValue.getValue() != null && stateValue.getValue().getValue() != null
        // && stateValue.getValue().getValue().equals("done") && entity.isValid()) {
        // formValue.setEnabled(false);
        // }
        //
        // if (formValue == null) {
        // return;
        // }
        //
        // if (defaultInstructionValue == null) {
        // defaultInstructionValue = new ViewValue<SimpleValue>(new SimpleValue(""));
        // defaultInstructionValue.setVisible(true);
        // formValue.addComponent("defaultInstruction", defaultInstructionValue);
        // }
        // if (plannedQuantityValue == null) {
        // plannedQuantityValue = new ViewValue<SimpleValue>(new SimpleValue(null));
        // plannedQuantityValue.setVisible(true);
        // plannedQuantityValue.setEnabled(true);
        // formValue.addComponent("plannedQuantity", plannedQuantityValue);
        // }
        //
        // defaultInstructionValue.setEnabled(false);
        // defaultInstructionValue.setValue(new SimpleValue(""));
        // instructionValue.setEnabled(true);
        //
        // Long selectedProductId = null;
        //
        // if (productValue.getValue() != null && productValue.getValue().getSelectedEntityId() != null) {
        // selectedProductId = productValue.getValue().getSelectedEntityId();
        // }
        //
        // Entity selectedInstruction = null;
        //
        // if (selectedProductId != null && instructionValue.getValue() != null
        // && instructionValue.getValue().getSelectedEntityId() != null
        // && !"mainWindow.orderDetailsForm.product".equals(triggerComponentName)) {
        // selectedInstruction = dataDefinitionService.get("products", "instruction").get(
        // instructionValue.getValue().getSelectedEntityId());
        // } else {
        // instructionValue.getValue().setSelectedEntityId(null);
        // instructionValue.getValue().setSelectedEntityCode("");
        // instructionValue.getValue().setSelectedEntityValue("");
        // }
        //
        // if (selectedProductId == null) {
        // instructionValue.setEnabled(false);
        // instructionValue.getValue().setSelectedEntityId(null);
        // instructionValue.getValue().setSelectedEntityCode("");
        // instructionValue.getValue().setSelectedEntityValue("");
        // instructionValue.getValue().setRequired(false);
        // plannedQuantityValue.getValue().setRequired(false);
        // } else {
        // plannedQuantityValue.getValue().setRequired(true);
        // if (!hasAnyInstructions(selectedProductId)) {
        // instructionValue.setEnabled(false);
        // instructionValue.getValue().setRequired(false);
        // instructionValue.getValue().setSelectedEntityId(null);
        // instructionValue.getValue().setSelectedEntityCode("");
        // instructionValue.getValue().setSelectedEntityValue("");
        // } else {
        // instructionValue.getValue().setRequired(true);
        // Entity defaultInstructionEntity = getDefaultInstruction(selectedProductId);
        // if (defaultInstructionEntity != null) {
        // String defaultInstructionName = defaultInstructionEntity.getField("name").toString();
        // defaultInstructionValue.getValue().setValue(defaultInstructionName);
        // if (selectedInstruction == null && "mainWindow.orderDetailsForm.product".equals(triggerComponentName)) {
        // selectDefaultInstruction(instructionValue, defaultInstructionEntity);
        // }
        // }
        // }
        // }
    }

    // private void selectDefaultInstruction(final ViewValue<LookupData> instructionValue, final Entity defaultInstructionEntity)
    // {
    // ViewDefinition viewDefinition = viewDefinitionService.get("products", "orderDetailsView");
    // LookupComponent lookupInstruction = (LookupComponent) viewDefinition
    // .lookupComponent("mainWindow.orderDetailsForm.instruction");
    // instructionValue.getValue().setValue(defaultInstructionEntity.getId());
    // instructionValue.getValue().setSelectedEntityCode(
    // defaultInstructionEntity.getStringField(lookupInstruction.getFieldCode()));
    // instructionValue.getValue().setSelectedEntityValue(
    // ExpressionUtil.getValue(defaultInstructionEntity, lookupInstruction.getExpression()));
    // }
    //
    // private Entity getDefaultInstruction(final Long selectedProductId) {
    // DataDefinition instructionDD = dataDefinitionService.get("products", "instruction");
    //
    // SearchCriteriaBuilder searchCriteria = instructionDD.find().withMaxResults(1)
    // .restrictedWith(Restrictions.eq(instructionDD.getField("master"), true))
    // .restrictedWith(Restrictions.belongsTo(instructionDD.getField("product"), selectedProductId));
    //
    // SearchResult searchResult = searchCriteria.list();
    //
    // if (searchResult.getTotalNumberOfEntities() == 1) {
    // return searchResult.getEntities().get(0);
    // } else {
    // return null;
    // }
    // }

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

    public void fillDateAndWorkerOnGenerate(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField("fileName") != null && !"".equals(entity.getField("fileName").toString().trim())) {
            entity.setField("generated", true);
        }
        if ((Boolean) entity.getField("generated") && entity.getField("date") == null) {
            entity.setField("date", new Date());
        }
        if ((Boolean) entity.getField("generated") && entity.getField("worker") == null) {
            entity.setField("worker", getLoginOfLoggedUser());
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
            state.performEvent(viewDefinitionState, "save", new String[0]);

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
                    materialRequirementPdfService.generateDocument(materialRequirement, state.getLocale());
                    materialRequirementXlsService.generateDocument(materialRequirement, state.getLocale());
                    state.performEvent(viewDefinitionState, "reset", new String[0]);
                } catch (IOException e) {
                    new IllegalStateException(e.getMessage(), e);
                } catch (DocumentException e) {
                    new IllegalStateException(e.getMessage(), e);
                }
            }
        }
    }

    public void printMaterialRequirement(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        GridComponentState gridState = (GridComponentState) state;
        if (gridState.getSelectedEntityId() == null) {
            gridState.addMessage("Nie ma takiego numeru", MessageType.FAILURE); // TODO mina i18n
        }

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
        FieldComponentState name = (FieldComponentState) state.getComponentByPath(("window.materialRequirement.name"));
        FieldComponentState onlyComponents = (FieldComponentState) state
                .getComponentByPath(("window.materialRequirement.onlyComponents"));
        FieldComponentState generated = (FieldComponentState) state.getComponentByPath(("window.materialRequirement.generated"));
        FieldComponentState materialRequirementComponents = (FieldComponentState) state
                .getComponentByPath(("window.materialRequirementComponents"));

        if ("1".equals(generated.getFieldValue())) {
            name.setEnabled(false);
            onlyComponents.setEnabled(false);
            materialRequirementComponents.setEnabled(false);
        }
    }

}
