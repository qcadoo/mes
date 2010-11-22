/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.RestrictionOperator;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.products.print.pdf.MaterialRequirementPdfService;
import com.qcadoo.mes.products.print.xls.MaterialRequirementXlsService;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.components.LookupComponent;
import com.qcadoo.mes.view.components.LookupData;
import com.qcadoo.mes.view.components.SimpleValue;
import com.qcadoo.mes.view.containers.FormValue;

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
    private TranslationService translationService;

    public boolean checkIfProductIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        ProductsProduct product = (ProductsProduct) entity.getField("product");

        Entity productEntity = dataDefinitionService.get("products", "product").get(product.getId());

        if (productEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("product", null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfSubstituteIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        ProductsSubstitute substitute = (ProductsSubstitute) entity.getField("substitute");

        Entity substituteEntity = dataDefinitionService.get("products", "substitute").get(substitute.getId());

        if (substituteEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("substitute", null);
            return false;
        } else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public void disableFormForExistingMaterialRequirement(final ViewValue<Long> value, final String triggerComponentName,
            final Locale locale) throws IOException, DocumentException {

        if (value.lookupValue("mainWindow.materialRequirementDetailsForm") == null
                || value.lookupValue("mainWindow.materialRequirementDetailsForm").getValue() == null
                || ((FormValue) value.lookupValue("mainWindow.materialRequirementDetailsForm").getValue()).getId() == null) {

            ViewValue<SimpleValue> onlyComponentsValue = (ViewValue<SimpleValue>) value
                    .lookupValue("mainWindow.materialRequirementDetailsForm.onlyComponents");
            if (onlyComponentsValue != null && onlyComponentsValue.getValue() != null) {
                onlyComponentsValue.getValue().setValue("1");
            }
            return;
        }

        String generatedStringValue = ((SimpleValue) value.lookupValue("mainWindow.materialRequirementDetailsForm.generated")
                .getValue()).getValue().toString();

        boolean isGenerated = true;
        if ("0".equals(generatedStringValue)) {
            isGenerated = false;
        }

        if (isGenerated) {
            value.lookupValue("mainWindow.materialRequirementDetailsForm.name").setEnabled(false);
            value.lookupValue("mainWindow.materialRequirementDetailsForm.onlyComponents").setEnabled(false);
            value.lookupValue("mainWindow.ordersGrid").setEnabled(false);

            if ("mainWindow.materialRequirementDetailsForm".equals(triggerComponentName)) {
                Entity materialRequirement = dataDefinitionService.get("products", "materialRequirement").get(
                        ((FormValue) value.lookupValue("mainWindow.materialRequirementDetailsForm").getValue()).getId());

                if (materialRequirement.getField("fileName") == null
                        || "".equals(materialRequirement.getField("fileName").toString().trim())) {
                    materialRequirementPdfService.generateDocument(materialRequirement, locale);
                    materialRequirementXlsService.generateDocument(materialRequirement, locale);
                } else {
                    // FIXME KRNA remove override
                    value.addInfoMessage("override:"
                            + translationService
                                    .translate(
                                            "products.materialRequirementDetailsView.mainWindow.materialRequirementDetailsForm.documentsWasGenerated",
                                            locale));
                }

            }
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

        if (searchResult.getTotalNumberOfEntities() == 0) {
            return true;
        } else {
            entity.addError(dataDefinition.getField("order"), "products.validate.global.error.materialRequirementDuplicated");
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void generateOrderNumber(final ViewValue<Long> value, final String triggerComponentName, final Locale locale) {
        ViewValue<FormValue> formValue = (ViewValue<FormValue>) value.lookupValue("mainWindow.orderDetailsForm");
        ViewValue<SimpleValue> numberValue = (ViewValue<SimpleValue>) value.lookupValue("mainWindow.orderDetailsForm.number");

        if (formValue == null || numberValue == null) {
            return;
        }

        if (formValue.getValue() != null && formValue.getValue().getId() != null) {
            // form is already saved
            return;
        }

        if (numberValue.getValue() != null && StringUtils.hasText((String) numberValue.getValue().getValue())) {
            // number is already choosen
            return;
        }

        if (numberValue.getMessages().size() > 0) {
            // there is a validation message for that field
            return;
        }

        SearchResult results = dataDefinitionService.get("products", "order").find().withMaxResults(1).includeDeleted().list();

        String number = String.format("%06d", results.getTotalNumberOfEntities() + 1);

        if (numberValue.getValue() == null) {
            numberValue.setValue(new SimpleValue(number));
        } else {
            numberValue.getValue().setValue(number);
        }
    }

    @SuppressWarnings("unchecked")
    public void afterOrderDetailsLoad(final ViewValue<Long> value, final String triggerComponentName, final Locale locale) {
        generateOrderNumber(value, triggerComponentName, locale);

        ViewValue<LookupData> productValue = (ViewValue<LookupData>) value.lookupValue("mainWindow.orderDetailsForm.product");
        ViewValue<SimpleValue> defaultInstructionValue = (ViewValue<SimpleValue>) value
                .lookupValue("mainWindow.orderDetailsForm.defaultInstruction");
        ViewValue<LookupData> instructionValue = (ViewValue<LookupData>) value
                .lookupValue("mainWindow.orderDetailsForm.instruction");
        ViewValue<SimpleValue> stateValue = (ViewValue<SimpleValue>) value.lookupValue("mainWindow.orderDetailsForm.state");
        ViewValue<FormValue> formValue = (ViewValue<FormValue>) value.lookupValue("mainWindow.orderDetailsForm");
        ViewValue<SimpleValue> plannedQuantityValue = (ViewValue<SimpleValue>) value
                .lookupValue("mainWindow.orderDetailsForm.plannedQuantity");

        if (stateValue != null && stateValue.getValue() != null && stateValue.getValue().getValue() != null
                && stateValue.getValue().getValue().equals("done")) {
            formValue.setEnabled(false);
        }

        if (formValue == null) {
            return;
        }

        if (defaultInstructionValue == null) {
            defaultInstructionValue = new ViewValue<SimpleValue>(new SimpleValue(""));
            defaultInstructionValue.setVisible(true);
            formValue.addComponent("defaultInstruction", defaultInstructionValue);
        }
        if (plannedQuantityValue == null) {
            plannedQuantityValue = new ViewValue<SimpleValue>(new SimpleValue(null));
            plannedQuantityValue.setVisible(true);
            plannedQuantityValue.setEnabled(true);
            formValue.addComponent("plannedQuantity", plannedQuantityValue);
        }

        defaultInstructionValue.setEnabled(false);
        defaultInstructionValue.setValue(new SimpleValue(""));
        instructionValue.setEnabled(true);

        Long selectedProductId = null;

        if (productValue.getValue() != null && productValue.getValue().getSelectedEntityId() != null) {
            selectedProductId = productValue.getValue().getSelectedEntityId();
        }

        Entity selectedInstruction = null;

        if (selectedProductId != null && instructionValue.getValue() != null
                && instructionValue.getValue().getSelectedEntityId() != null
                && !"mainWindow.orderDetailsForm.product".equals(triggerComponentName)) {
            selectedInstruction = dataDefinitionService.get("products", "instruction").get(
                    instructionValue.getValue().getSelectedEntityId());
        } else {
            instructionValue.getValue().setSelectedEntityId(null);
            instructionValue.getValue().setSelectedEntityCode("");
            instructionValue.getValue().setSelectedEntityValue("");
        }

        if (selectedProductId == null) {
            instructionValue.setEnabled(false);
            instructionValue.getValue().setSelectedEntityId(null);
            instructionValue.getValue().setSelectedEntityCode("");
            instructionValue.getValue().setSelectedEntityValue("");
            instructionValue.getValue().setRequired(false);
            plannedQuantityValue.getValue().setRequired(false);
        } else {
            plannedQuantityValue.getValue().setRequired(true);
            if (!hasAnyInstructions(selectedProductId)) {
                instructionValue.setEnabled(false);
                instructionValue.getValue().setRequired(false);
                instructionValue.getValue().setSelectedEntityId(null);
                instructionValue.getValue().setSelectedEntityCode("");
                instructionValue.getValue().setSelectedEntityValue("");
            } else {
                instructionValue.getValue().setRequired(true);
                Entity defaultInstructionEntity = getDefaultInstruction(selectedProductId);
                if (defaultInstructionEntity != null) {
                    String defaultInstructionName = defaultInstructionEntity.getField("name").toString();
                    defaultInstructionValue.getValue().setValue(defaultInstructionName);
                    if (selectedInstruction == null && "mainWindow.orderDetailsForm.product".equals(triggerComponentName)) {
                        selectDefaultInstruction(instructionValue, defaultInstructionEntity);
                    }
                }
            }
        }
    }

    private void selectDefaultInstruction(final ViewValue<LookupData> instructionValue, final Entity defaultInstructionEntity) {
        ViewDefinition viewDefinition = viewDefinitionService.get("products", "orderDetailsView");
        LookupComponent lookupInstruction = (LookupComponent) viewDefinition
                .lookupComponent("mainWindow.orderDetailsForm.instruction");
        instructionValue.getValue().setValue(defaultInstructionEntity.getId());
        instructionValue.getValue().setSelectedEntityCode(
                defaultInstructionEntity.getStringField(lookupInstruction.getFieldCode()));
        instructionValue.getValue().setSelectedEntityValue(
                ExpressionUtil.getValue(defaultInstructionEntity, lookupInstruction.getExpression()));
    }

    private Entity getDefaultInstruction(final Long selectedProductId) {
        DataDefinition instructionDD = dataDefinitionService.get("products", "instruction");

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

    private boolean hasAnyInstructions(final Long selectedProductId) {
        DataDefinition instructionDD = dataDefinitionService.get("products", "instruction");

        SearchCriteriaBuilder searchCriteria = instructionDD.find().withMaxResults(1)
                .restrictedWith(Restrictions.belongsTo(instructionDD.getField("product"), selectedProductId));

        SearchResult searchResult = searchCriteria.list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

    public boolean checkInstructionDefault(final DataDefinition dataDefinition, final Entity entity) {
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
            entity.addError(dataDefinition.getField("plannedQuantity"), "products.validate.global.error.plannedQuantityError");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkOrderInstruction(final DataDefinition dataDefinition, final Entity entity) {
        ProductsProduct product = (ProductsProduct) entity.getField("product");
        if (product == null) {
            return true;
        }
        if (entity.getField("instruction") == null) {
            if (hasAnyInstructions(product.getId())) {
                entity.addError(dataDefinition.getField("instruction"), "products.validate.global.error.instructionError");
                return false;
            }
        }
        return true;
    }

    public boolean checkSubstituteDates(final DataDefinition dataDefinition, final Entity entity) {
        return compareDates(dataDefinition, entity, "effectiveDateFrom", "effectiveDateTo");
    }

    public boolean checkOrderDates(final DataDefinition dataDefinition, final Entity entity) {
        return compareDates(dataDefinition, entity, "dateFrom", "dateTo");
    }

    public boolean checkInstructionDates(final DataDefinition dataDefinition, final Entity entity) {
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

        if (entity.getField("effectiveDateTo") != null) {
            entity.setField("state", "done");
        } else if (entity.getField("effectiveDateFrom") != null) {
            entity.setField("state", "pending");
        }
    }

    public void fillMaterialRequirementDateAndWorker(final DataDefinition dataDefinition, final Entity entity) {
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

}
