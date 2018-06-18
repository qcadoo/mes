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
package com.qcadoo.mes.technologies.listeners;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.hooks.TOCDetailsHooks;
import com.qcadoo.mes.technologies.hooks.TechnologyOperationComponentHooks;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TOCDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE = "operationProductInComponentsSource";

    private static final String L_LINE_SEPARATOR = "line.separator";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_IS_EMPTY = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.isEmpty";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRECT_LINE = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.incorrectLine";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRECT_QUANTITY = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.incorrectQuantity";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRCT_NUMBER = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.incorrectNumber";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_PRODUCT_HAS_ALREADY_BEEN_ADDED = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.productHasAlreadyBeenAdded";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_INFO = "technologies.technologyOperationComponent.import.info";

    public static final int BASE_IMPORT_POSITIONS_LENGTH = 2;

    public static final int EXTEND_IMPORT_POSITIONS_LENGTH = 3;

    private enum ErrorMessageType {
        INCORRECT_LINE(L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRECT_LINE), INCORRECT_NUMBER(
                L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRCT_NUMBER);

        private final String errorMessageType;

        private ErrorMessageType(final String errorMessageType) {
            this.errorMessageType = errorMessageType;
        }

        public String getStringValue() {
            return errorMessageType;
        }

    }

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TOCDetailsHooks tOCDetailsHooks;

    @Autowired
    private TechnologyOperationComponentHooks technologyOperationComponentHooks;

    public void copyWorkstationsSettingsFromOperation(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);
        Entity toc = formComponent.getEntity();
        technologyOperationComponentHooks.copyWorkstationsSettingsFromOperation(toc);
        formComponent.setEntity(toc);
    }

    public void setProductionLineLookup(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        tOCDetailsHooks.setProductionLineLookup(view);
    }

    public void setWorkstationsLookup(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        tOCDetailsHooks.setWorkstationsLookup(view);
    }

    public void setWorkstationsTabFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        tOCDetailsHooks.setWorkstationsTabFields(view);
        FieldComponent assignedToOperation = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();
        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.WORKSTATION_TYPE);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.PRODUCTION_LINE);
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            tOCDetailsHooks.clearWorkstationsField(view);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.PRODUCTION_LINE);
        }

    }

    public void addUpTheNumberOfWorktations(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        int size = form.getPersistedEntityWithIncludedFormValues()
                .getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS).size();
        FieldComponent quantityOfWorkstations = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS);
        quantityOfWorkstations.setFieldValue(size);
        quantityOfWorkstations.requestComponentUpdateState();
    }

    public void importOperationProductInComponents(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent technologyOperationComponentForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent operationProductInComponentsSourceField = (FieldComponent) view
                .getComponentByReference(L_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE);

        Long technologyOperationComponentId = technologyOperationComponentForm.getEntityId();

        if (technologyOperationComponentId == null) {
            return;
        }

        String operationProductInComponentsSource = (String) operationProductInComponentsSourceField.getFieldValue();

        if (StringUtils.isEmpty(operationProductInComponentsSource)) {
            technologyOperationComponentForm.addMessage(
                    L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_IS_EMPTY,
                    ComponentState.MessageType.INFO);
        } else {
            Map<ErrorMessageType, Set<String>> errorMessages = Maps.newHashMap();

            int importedCount = parseOperationProductInComponentsSource(technologyOperationComponentForm,
                    operationProductInComponentsSource, errorMessages);

            showErrorMessages(technologyOperationComponentForm, errorMessages);

            if (importedCount > 0) {
                technologyOperationComponentForm.addMessage(L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_INFO,
                        ComponentState.MessageType.SUCCESS, Integer.toString(importedCount));
            } else {
                technologyOperationComponentForm.addMessage(L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_INFO,
                        ComponentState.MessageType.INFO, Integer.toString(importedCount));
            }
        }
    }

    private int parseOperationProductInComponentsSource(final FormComponent technologyOperationComponentForm,
            final String operationProductInComponentsSource, final Map<ErrorMessageType, Set<String>> errorMessages) {
        int importedCount = 0;

        String[] operationProductInComponentsSourceLines = operationProductInComponentsSource.split(System
                .getProperty(L_LINE_SEPARATOR));

        for (String operationProductInComponentsSourceLine : operationProductInComponentsSourceLines) {
            if (StringUtils.isNotEmpty(operationProductInComponentsSourceLine)) {
                importedCount += parseOperationProductInComponentsSourceLine(technologyOperationComponentForm,
                        operationProductInComponentsSourceLine, errorMessages);
            }
        }

        return importedCount;
    }

    private int parseOperationProductInComponentsSourceLine(final FormComponent technologyOperationComponentForm,
            final String operationProductInComponentsSourceLine, final Map<ErrorMessageType, Set<String>> errorMessages) {
        int imported = 0;

        String[] operationProductInComponentSource = operationProductInComponentsSourceLine.split("\\|");

        if (operationProductInComponentSource.length == BASE_IMPORT_POSITIONS_LENGTH
                || operationProductInComponentSource.length == EXTEND_IMPORT_POSITIONS_LENGTH) {
            String operationProductInComponentNumber = StringUtils.strip(operationProductInComponentSource[0]);
            String operationProductInComponentQuantity = StringUtils.strip(operationProductInComponentSource[1]);

            String operationProductInComponentItemNumberInTheExplodedView = StringUtils.EMPTY;
            if (operationProductInComponentSource.length == EXTEND_IMPORT_POSITIONS_LENGTH) {
                operationProductInComponentItemNumberInTheExplodedView = StringUtils.strip(operationProductInComponentSource[2]);
            }

            if (StringUtils.isNotEmpty(operationProductInComponentNumber)
                    && StringUtils.isNotEmpty(operationProductInComponentQuantity)) {
                imported = parseOperationProductInComponentSource(technologyOperationComponentForm,
                        operationProductInComponentNumber, operationProductInComponentQuantity,
                        operationProductInComponentItemNumberInTheExplodedView, errorMessages);
            } else {
                updateErrorMessages(errorMessages, ErrorMessageType.INCORRECT_LINE, operationProductInComponentsSourceLine);
            }
        } else {
            updateErrorMessages(errorMessages, ErrorMessageType.INCORRECT_LINE, operationProductInComponentsSourceLine);
        }

        return imported;
    }

    private int parseOperationProductInComponentSource(final FormComponent technologyOperationComponentForm,
            final String operationProductInComponentNumber, final String operationProductInComponentQuantity,
            final String operationProductInComponentItemNumberInTheExplodedView,
            final Map<ErrorMessageType, Set<String>> errorMessages) {
        int imported = 0;
        boolean canImport = true;

        Entity technologyOperationComponent = getTechnologyOperationComponentFromDB(technologyOperationComponentForm.getEntity());
        Entity product = getProductWithGivenNumberFromDB(operationProductInComponentNumber);
        BigDecimal quantity = null;

        if (product == null) {
            updateErrorMessages(errorMessages, ErrorMessageType.INCORRECT_NUMBER, operationProductInComponentNumber);

            canImport = false;
        }

        NumberFormat numberFormat = NumberFormat.getInstance(LocaleContextHolder.getLocale());

        try {
            quantity = new BigDecimal(numberFormat.parse(operationProductInComponentQuantity).toString());
        } catch (ParseException e) {
            technologyOperationComponentForm
                    .addMessage(
                            L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRECT_QUANTITY,
                            ComponentState.MessageType.FAILURE, operationProductInComponentQuantity);

            canImport = false;
        }

        String itemNumberInTheExplodedView = operationProductInComponentItemNumberInTheExplodedView;

        if (canImport) {
            if (checkIfProductHasAlreadyBeenAdded(technologyOperationComponent, product)) {
                technologyOperationComponentForm
                        .addMessage(
                                L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_PRODUCT_HAS_ALREADY_BEEN_ADDED,
                                ComponentState.MessageType.FAILURE, operationProductInComponentNumber);
            } else {
                createOperationProductInComponent(technologyOperationComponent, product, quantity, itemNumberInTheExplodedView);

                imported = 1;
            }
        }

        return imported;
    }

    private boolean checkIfProductHasAlreadyBeenAdded(final Entity technologyOperationComponent, final Entity product) {
        SearchResult searchResult = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)
                .find()
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.OPERATION_COMPONENT,
                        technologyOperationComponent))
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, product)).list();

        return !searchResult.getEntities().isEmpty();
    }

    private void createOperationProductInComponent(final Entity technologyOperationComponent, final Entity product,
            final BigDecimal quantity, final String itemNumberInTheExplodedView) {
        Entity operationProductInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();

        operationProductInComponent.setField(OperationProductInComponentFields.OPERATION_COMPONENT, technologyOperationComponent);
        operationProductInComponent.setField(OperationProductInComponentFields.PRODUCT, product);
        operationProductInComponent.setField(OperationProductInComponentFields.QUANTITY,
                numberService.setScaleWithDefaultMathContext(quantity));
        operationProductInComponent.setField(OperationProductInComponentFields.ITEM_NUMBER_IN_THE_EXPLODED_VIEW,
                itemNumberInTheExplodedView);

        operationProductInComponent.getDataDefinition().save(operationProductInComponent);
    }

    private Entity getTechnologyOperationComponentFromDB(final Entity technologyOperationComponent) {
        return technologyOperationComponent.getDataDefinition().get(technologyOperationComponent.getId());
    }

    private Entity getProductWithGivenNumberFromDB(final String number) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).find()
                .add(SearchRestrictions.eq(ProductFields.NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private void updateErrorMessages(final Map<ErrorMessageType, Set<String>> errorMessages, final ErrorMessageType errorType,
            final String errorMessageArg) {
        if (errorMessages.containsKey(errorType)) {
            Set<String> errorMessageArgs = errorMessages.get(errorType);

            errorMessageArgs.add(errorMessageArg);

            errorMessages.put(errorType, errorMessageArgs);
        } else {
            errorMessages.put(errorType, Sets.newHashSet(errorMessageArg));
        }
    }

    private void showErrorMessages(final FormComponent technologyOperationComponentForm,
            final Map<ErrorMessageType, Set<String>> errorMessages) {
        if (!errorMessages.isEmpty()) {
            for (Map.Entry<ErrorMessageType, Set<String>> errorMessage : errorMessages.entrySet()) {
                ErrorMessageType errorMessageType = errorMessage.getKey();
                Set<String> errorMessageArgs = errorMessage.getValue();

                if (!errorMessageArgs.isEmpty()) {
                    StringBuilder args = new StringBuilder();
                    for (String errorMessageArg : errorMessageArgs) {
                        if (args.length() > 0) {
                            if (ErrorMessageType.INCORRECT_LINE.equals(errorMessageType)) {
                                args.append("<br/>");
                            } else {
                                args.append(", ");
                            }
                        }

                        args.append(errorMessageArg);
                    }

                    technologyOperationComponentForm.addMessage(errorMessageType.getStringValue(),
                            ComponentState.MessageType.FAILURE, args.toString());
                }
            }
        }
    }

}
