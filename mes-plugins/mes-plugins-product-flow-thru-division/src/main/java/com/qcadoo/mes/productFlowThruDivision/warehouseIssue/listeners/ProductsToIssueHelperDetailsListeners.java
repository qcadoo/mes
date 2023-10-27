package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.exceptions.DocumentBuildException;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.service.WarehouseIssueService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.UpdateIssuesLocationsQuantityStatusHolder;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks.ProductsToIssueDetailsHooks;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.validators.IssueValidators;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.exception.RuntimeExceptionWithArguments;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.model.api.validators.GlobalMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ProductsToIssueHelperDetailsListeners {

    private static final String L_ISSUES = "issues";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ProductsToIssueDetailsHooks productsToIssueDetailsHooks;

    @Autowired
    private WarehouseIssueService warehouseIssueService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private IssueValidators issueValidators;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    @Transactional
    private List<Entity> createDocumentsInvoke(final ViewDefinitionState view, final Entity helper) {
        Optional<List<Entity>> optionalPositions = saveProductsToIssue(view);

        Optional<String> additionalInfo = Optional.ofNullable(helper.getStringField("additionalInfo"));

        if (additionalInfo.isPresent() && additionalInfo.get().length() > 1024) {
            throw new RuntimeException("productFlowThruDivision.productsToIssueHelperDetails.error.additionalInfoTooLong");
        }

        if (optionalPositions.isPresent()) {
            if (!issueValidators.checkIfCanIssueQuantity(optionalPositions.get())) {
                throw new RuntimeException("productFlowThruDivision.issue.state.accept.error.issuedToExtentNecessary");
            }

            List<Entity> documents = warehouseIssueService.createWarehouseDocumentsForPositions(view, optionalPositions.get(),
                    helper.getBelongsToField("locationFrom"), additionalInfo);

            return documents;
        } else {
            throw new RuntimeException("productFlowThruDivision.productsToIssueHelperDetails.window.save.error");
        }
    }

    public void createDocuments(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity helper = form.getPersistedEntityWithIncludedFormValues();

        FieldComponent helperField = (FieldComponent) view.getComponentByReference("generated");

        boolean success = false;

        try {
            List<Entity> documents = createDocumentsInvoke(view, helper);

            afterSuccessfulCreateDocumentsHooks(view, documents);

            success = true;

        } catch (DocumentBuildException e) {
            e.getInvalidPositions().forEach(ip -> copyPositionMessages(ip, view));
        } catch (EntityRuntimeException e) {
            copyMessages(e.getEntity(), view);
        } catch (RuntimeExceptionWithArguments e) {
            view.addMessage(e.getMessage(), ComponentState.MessageType.FAILURE, e.getArguments());
        } catch (Exception e) {
            view.addMessage(e.getMessage(), ComponentState.MessageType.FAILURE);
        }

        helperField.setFieldValue(success);
        helperField.requestComponentUpdateState();
    }

    public void createDocumentsAndGoBack(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent goBackCheckBox = (CheckBoxComponent) view.getComponentByReference("goBack");
        FieldComponent generatedField = (FieldComponent) view.getComponentByReference("generated");

        createDocuments(view, state, args);

        Boolean generated = Boolean.valueOf(generatedField.getFieldValue().toString());

        goBackCheckBox.setChecked(BooleanUtils.isTrue(generated));
    }

    private void copyMessages(Entity entity, final ViewDefinitionState componentMessagesHolder) {
        if (Objects.isNull(componentMessagesHolder)) {
            return;
        }

        for (ErrorMessage errorMessage : entity.getGlobalErrors()) {
            componentMessagesHolder.addMessage(errorMessage);
        }
        for (ErrorMessage errorMessage : entity.getErrors().values()) {
            componentMessagesHolder.addMessage(errorMessage);
        }

        for (GlobalMessage globalMessage : entity.getGlobalMessages()) {
            componentMessagesHolder.addMessage(globalMessage);
        }
    }

    private void copyPositionMessages(Entity invalidPosition, final ViewDefinitionState componentMessagesHolder) {
        if (Objects.isNull(componentMessagesHolder)) {
            return;
        }

        Locale locale = LocaleContextHolder.getLocale();

        String productNumber = Optional.of(invalidPosition).map(ip -> ip.getBelongsToField(PositionFields.PRODUCT))
                .map(p -> p.getStringField(ProductFields.NUMBER)).orElse("???");

        for (ErrorMessage errorMessage : invalidPosition.getGlobalErrors()) {
            String translatedMessage = translationService.translate(errorMessage.getMessage(), locale, errorMessage.getVars());
            translatedMessage = translationService.translate("productFlowThruDivision.issue.documentBuild.position.error", locale,
                    translatedMessage, productNumber);

            componentMessagesHolder.addTranslatedMessage(translatedMessage, ComponentState.MessageType.FAILURE,
                    errorMessage.getAutoClose(), errorMessage.isExtraLarge());
        }
        for (ErrorMessage errorMessage : invalidPosition.getErrors().values()) {
            String translatedMessage = translationService.translate(errorMessage.getMessage(), locale, errorMessage.getVars());
            translatedMessage = translationService.translate("productFlowThruDivision.issue.documentBuild.position.error", locale,
                    translatedMessage, productNumber);

            componentMessagesHolder.addTranslatedMessage(translatedMessage, ComponentState.MessageType.FAILURE,
                    errorMessage.getAutoClose(), errorMessage.isExtraLarge());
        }
    }

    private void createDocument(final ViewDefinitionState view, final List<Entity> positions) {
        view.addMessage("productFlowThruDivision.productsToIssueHelperDetails.window.save.error",
                ComponentState.MessageType.FAILURE);
    }

    private Optional<List<Entity>> saveProductsToIssue(final ViewDefinitionState view) {
        List<Entity> savedEntities = Lists.newArrayList();

        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(L_ISSUES);

        DataDefinition issueDD = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_ISSUE);

        boolean areErrors = false;

        Map<FormComponent, Entity> entityToSaved = new HashMap<>();

        for (FormComponent formComponent : adl.getFormComponents()) {
            Entity issue = formComponent.getPersistedEntityWithIncludedFormValues();

            issue.setField(IssueFields.ISSUED, false);

            Entity saved = issueDD.save(issue);

            if (!saved.isValid()) {
                Map<String, ErrorMessage> errorMessageMap = saved.getErrors();

                errorMessageMap.forEach((field, errorMessage) -> formComponent.findFieldComponentByName(field).addMessage(errorMessage.getMessage(), ComponentState.MessageType.FAILURE));

                areErrors = true;
            } else {
                if (Objects.nonNull(saved.getDecimalField(IssueFields.ISSUE_QUANTITY))) {
                    if (saved.getDecimalField(IssueFields.ISSUE_QUANTITY).compareTo(BigDecimal.valueOf(0)) == 0) {
                        formComponent.findFieldComponentByName(IssueFields.ISSUE_QUANTITY).addMessage("productFlowThruDivision.productsToIssueHelperDetails.issueQuantity", ComponentState.MessageType.FAILURE);

                        areErrors = true;
                    } else {
                        entityToSaved.put(formComponent, saved);
                    }
                }
            }

            savedEntities.add(saved);
        }

        if (areErrors) {
            return Optional.empty();
        }
        if (!checkIssuedToExtentNecessary(savedEntities, adl)) {
            return Optional.empty();
        }
        if (!checkNoProductsOnLocation(savedEntities, view, adl)) {
            return Optional.empty();
        }

        entityToSaved.forEach(FormComponent::setEntity);

        return Optional.of(savedEntities);
    }

    public void fillUnits(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productsToIssueDetailsHooks.fillUnits(view);
    }

    public void updateQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(L_ISSUES);

        Long changedId = (Long) state.getFieldValue();

        for (FormComponent form : adl.getFormComponents()) {
            Entity issue = form.getPersistedEntityWithIncludedFormValues();

            Entity product = issue.getBelongsToField(IssueFields.PRODUCT);

            if (Objects.nonNull(product) && product.getId().equals(changedId)) {
                Entity locationTo = issue.getBelongsToField(IssueFields.LOCATION);

                Entity warehouseIssue = issue.getBelongsToField(IssueFields.WAREHOUSE_ISSUE);
                Entity locationFrom = warehouseIssue.getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE);

                Optional<Entity> maybeProductToIssue = warehouseIssue.getHasManyField(WarehouseIssueFields.PRODUCTS_TO_ISSUES)
                        .stream()
                        .filter(p -> p.getBelongsToField(ProductsToIssueFields.PRODUCT).getId().equals(product.getId())
                                && p.getBelongsToField(ProductsToIssueFields.LOCATION).getId().equals(locationTo.getId()))
                        .findFirst();

                BigDecimal quantityFrom = materialFlowResourcesService.getResourcesQuantityForLocationAndProduct(locationFrom,
                        product);
                BigDecimal quantityTo = materialFlowResourcesService.getResourcesQuantityForLocationAndProduct(locationTo,
                        product);

                if (maybeProductToIssue.isPresent()) {
                    Entity productToIssue = maybeProductToIssue.get();

                    BigDecimal demandQuantity = productToIssue.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
                    BigDecimal issuedQuantity = productToIssue.getDecimalField(ProductsToIssueFields.ISSUE_QUANTITY);
                    BigDecimal quantityPerUnit = null;
                    BigDecimal issueQuantity = BigDecimal.ZERO;

                    if (warehouseIssueParameterService.issueForOrder()) {
                        Entity order = warehouseIssue.getBelongsToField(WarehouseIssueFields.ORDER);
                        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

                        if (Objects.nonNull(plannedQuantity) && Objects.nonNull(demandQuantity)) {
                            quantityPerUnit = demandQuantity.divide(plannedQuantity, numberService.getMathContext());
                        }
                    }

                    if (Objects.nonNull(demandQuantity) && Objects.nonNull(issuedQuantity)) {
                        issueQuantity = demandQuantity.subtract(issuedQuantity);
                    }

                    if (issueQuantity.compareTo(BigDecimal.ZERO) == -1) {
                        issueQuantity = BigDecimal.ZERO;
                    }

                    issue.setField(IssueFields.DEMAND_QUANTITY, demandQuantity);
                    issue.setField(IssueFields.QUANTITY_PER_UNIT, quantityPerUnit);
                    issue.setField(IssueFields.ISSUE_QUANTITY, issueQuantity);

                    BigDecimal conversion = productToIssue.getDecimalField(ProductsToIssueFields.CONVERSION);

                    if (Objects.nonNull(conversion)) {
                        issue.setField(IssueFields.CONVERSION, conversion);

                        String unit = product.getStringField(ProductFields.UNIT);
                        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

                        BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(demandQuantity,
                                conversion, Optional.ofNullable(additionalUnit).orElse(unit));

                        issue.setField(IssueFields.ADDITIONAL_DEMAND_QUANTITY, newAdditionalQuantity);
                    }
                } else {
                    issue.setField(IssueFields.DEMAND_QUANTITY, BigDecimal.ZERO);
                    issue.setField(IssueFields.QUANTITY_PER_UNIT, BigDecimal.ZERO);
                }

                issue.setField(IssueFields.LOCATIONS_QUANTITY, quantityFrom);
                issue.setField(IssueFields.LOCATION_TO_QUANTITY, quantityTo);

                form.setEntity(issue);
            }
        }
    }

    public void quantityChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("issues");

        List<FormComponent> formComponents = adl.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity formEntity = formComponent.getPersistedEntityWithIncludedFormValues();

            FieldComponent quantityField = formComponent.findFieldComponentByName("issueQuantity");

            BigDecimal conversion = formEntity.getDecimalField(IssueFields.CONVERSION);
            FieldComponent additionalQuantity = formComponent.findFieldComponentByName("issueQuantityAdditionalUnit");

            if (quantityField.getUuid().equals(state.getUuid())) {
                Either<Exception, com.google.common.base.Optional<BigDecimal>> maybeQuantity = BigDecimalUtils
                        .tryParse(quantityField.getFieldValue().toString(), LocaleContextHolder.getLocale());

                if (Objects.nonNull(conversion) && maybeQuantity.isRight() && maybeQuantity.getRight().isPresent()) {
                    BigDecimal quantity = maybeQuantity.getRight().get();
                    BigDecimal newAdditionalQuantity = quantity.multiply(conversion, numberService.getMathContext());

                    newAdditionalQuantity = newAdditionalQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                            RoundingMode.HALF_UP);

                    additionalQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
                    additionalQuantity.requestComponentUpdateState();
                } else if (maybeQuantity.isLeft()) {
                    quantityField.setFieldValue(additionalQuantity.getFieldValue());
                    quantityField.addMessage("productFlowThruDivision.productsToIssueHelperDetails.error.invalidQuantity",
                            ComponentState.MessageType.FAILURE);
                }
            }
        }
    }

    public void additionalQuantityChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("issues");

        List<FormComponent> formComponents = adl.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            FieldComponent additionalQuantityField = formComponent.findFieldComponentByName("issueQuantityAdditionalUnit");

            Entity formEntity = formComponent.getPersistedEntityWithIncludedFormValues();

            BigDecimal conversion = formEntity.getDecimalField(IssueFields.CONVERSION);

            FieldComponent quantity = formComponent.findFieldComponentByName("issueQuantity");

            if (additionalQuantityField.getUuid().equals(state.getUuid())) {
                Either<Exception, com.google.common.base.Optional<BigDecimal>> maybeAdditionalQuantity = BigDecimalUtils
                        .tryParse(additionalQuantityField.getFieldValue().toString(), LocaleContextHolder.getLocale());

                if (Objects.nonNull(conversion) && maybeAdditionalQuantity.isRight() && maybeAdditionalQuantity.getRight().isPresent()) {
                    BigDecimal additionalQuantity = maybeAdditionalQuantity.getRight().get();
                    BigDecimal newQuantity = additionalQuantity.divide(conversion, numberService.getMathContext());

                    newQuantity = newQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                            RoundingMode.HALF_UP);

                    quantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newQuantity, 0));
                    quantity.requestComponentUpdateState();
                } else if (maybeAdditionalQuantity.isLeft()) {
                    additionalQuantityField.setFieldValue(quantity.getFieldValue());
                    additionalQuantityField.addMessage(
                            "productFlowThruDivision.productsToIssueHelperDetails.error.invalidQuantity",
                            ComponentState.MessageType.FAILURE);
                }
            }
        }
    }

    private void afterSuccessfulCreateDocumentsHooks(final ViewDefinitionState view, final List<Entity> documents) {
        // used in AOP
    }

    private boolean checkIssuedToExtentNecessary(final List<Entity> savedEntities, final AwesomeDynamicListComponent adl) {
        boolean canIssueQuantity = issueValidators.checkIfCanIssueQuantity(Optional.of(savedEntities).get());

        if (!canIssueQuantity) {
            adl.addMessage("productFlowThruDivision.issue.state.accept.error.issuedToExtentNecessary",
                    ComponentState.MessageType.FAILURE);

            return false;
        }

        return true;
    }

    private boolean checkNoProductsOnLocation(final List<Entity> savedEntities, final ViewDefinitionState view, final AwesomeDynamicListComponent adl) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity location = form.getEntity().getBelongsToField("locationFrom");

        UpdateIssuesLocationsQuantityStatusHolder updateIssuesStatus = warehouseIssueService
                .tryUpdateIssuesLocationsQuantity(location, Optional.of(savedEntities).get());

        if (!updateIssuesStatus.isUpdated()) {
            adl.addMessage("productFlowThruDivision.issue.state.accept.error.noProductsOnLocation",
                    ComponentState.MessageType.FAILURE, updateIssuesStatus.getMessage(),
                    location.getStringField(LocationFields.NUMBER));

            return false;
        }

        return true;
    }

    public void onDeleteRow(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("issues");

        if (adl.getFormComponents().isEmpty()) {
            WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
            window.getRibbon().getGroups().stream().filter(g -> !g.getName().equals("navigation"))
                    .flatMap(g -> g.getItems().stream()).forEach(i -> {
                        i.setEnabled(false);
                        i.requestUpdate(true);
                    });
        }
    }

}
