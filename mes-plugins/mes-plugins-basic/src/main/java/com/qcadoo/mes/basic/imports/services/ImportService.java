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
package com.qcadoo.mes.basic.imports.services;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.LogService;
import com.qcadoo.mes.basic.constants.LogFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.dtos.ImportError;
import com.qcadoo.mes.basic.imports.dtos.ImportStatus;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public abstract class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    private static final String L_REDIRECT_TO_LOGS = "redirectToLogs";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_WINDOW_SHOW_BACK = "window.showBack";

    public static final String L_IMPORT = "import";

    private static final String L_IMPORT_FILE = "importFile";

    private static final String L_IMPORTED = "imported";

    private static final String L_SHOULD_UPDATE = "shouldUpdate";

    private static final String L_SHOULD_SKIP = "shouldSkip";

    private static final String L_RESOURCES = "resources";

    private static final String L_BASIC_IMPORT_ERROR_FILE_REQUIRED = "basic.import.error.file.required";

    private static final String L_BASIC_IMPORT_ERROR_FILE_INVALID = "basic.import.error.file.invalid";

    private static final String L_BASIC_IMPORT_ERROR_FILE_EMPTY = "basic.import.error.file.empty";

    private static final String L_BASIC_IMPORT_SUCCESS_MESSAGE = "basic.import.success.message";

    private static final String L_BASIC_IMPORT_FAILURE_MESSAGE = "basic.import.failure.message";

    private static final String L_BASIC_IMPORT_ERROR_GENERIC = "basic.import.error.generic";

    private static final String L_BASIC_IMPORT_ERROR_ACTION_SAVE = "basic.import.error.action.save";

    private static final String L_BASIC_IMPORT_ERROR_LINE_NUMBER = "basic.import.error.line.number";

    public static final String L_DOT = ".";

    private static final String L_DASH = "_";

    private static final String L_SLASH = "/";

    private static final String L_IMPORT_SCHEMA = "ImportSchema";

    public static final String L_LABEL = "label";

    public static final String L_CSV = "csv";

    public static final String L_XLSX = "xlsx";


    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private LogService logService;

    public void downloadImportSchema(final ViewDefinitionState view, final String pluginIdentifier, final String modelName,
            final String extension) {
        String fileName = modelName + L_IMPORT_SCHEMA + L_DASH + LocaleContextHolder.getLocale().getLanguage() + L_DOT
                + extension;
        String redirectToUrl = UriComponentsBuilder.newInstance().path(L_SLASH).pathSegment(pluginIdentifier)
                .pathSegment(L_RESOURCES).pathSegment(fileName).build().toUriString();

        view.redirectTo(redirectToUrl, true, false);
    }

    public void processImportFile(final ViewDefinitionState view, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName) throws IOException {
        processImportFile(view, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, null, null);
    }

    public void processImportFile(final ViewDefinitionState view, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName, final Entity belongsTo,
            final String belongsToName) throws IOException {
        processImportFile(view, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, belongsTo, belongsToName, null,
                null);
    }

    public void processImportFile(final ViewDefinitionState view, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName, final Entity belongsTo,
            final String belongsToName, final Function<Entity, SearchCriterion> criteriaSupplier) throws IOException {
        processImportFile(view, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, belongsTo, belongsToName,
                criteriaSupplier, null);
    }

    public void processImportFile(final ViewDefinitionState view, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName,
            final Function<Entity, SearchCriterion> criteriaSupplier) throws IOException {
        processImportFile(view, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, null, null, criteriaSupplier,
                null);
    }

    public void processImportFile(final ViewDefinitionState view, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName, final Entity belongsTo,
            final String belongsToName, final Function<Entity, SearchCriterion> criteriaSupplier,
            final Function<Entity, Boolean> checkOnUpdate) throws IOException {
        boolean shouldUpdate = shouldUpdate(view);
        boolean shouldSkip = shouldSkip(view);

        FieldComponent importFileField = (FieldComponent) view.getComponentByReference(L_IMPORT_FILE);
        CheckBoxComponent importedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_IMPORTED);

        String filePath = (String) importFileField.getFieldValue();

        changeButtonsState(view, false);

        if (StringUtils.isBlank(filePath)) {
            view.addMessage(L_BASIC_IMPORT_ERROR_FILE_REQUIRED, ComponentState.MessageType.FAILURE);
        } else if (!checkFileExtension(filePath)) {
            view.addMessage(L_BASIC_IMPORT_ERROR_FILE_INVALID, ComponentState.MessageType.FAILURE);
        } else {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                ImportStatus importStatus = importFile(fis, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName,
                        belongsTo, belongsToName, shouldUpdate, criteriaSupplier, checkOnUpdate, shouldSkip);

                Integer rowsProcessed = importStatus.getRowsProcessed();
                Integer rowsWithErrors = importStatus.getErrorsSize();
                Integer savedEntities = rowsProcessed - rowsWithErrors;

                if (importStatus.hasErrors()) {
                    if (!rollbackOnError && (savedEntities > 0)) {
                        view.addMessage(L_BASIC_IMPORT_SUCCESS_MESSAGE, ComponentState.MessageType.SUCCESS, false,
                                String.valueOf(savedEntities));

                        importedCheckBox.setChecked(true);
                    }

                    view.addMessage(L_BASIC_IMPORT_FAILURE_MESSAGE, ComponentState.MessageType.FAILURE, false,
                            String.valueOf(rowsWithErrors));

                    propagateErrors(importStatus, cellBinderRegistry, filePath, pluginIdentifier, modelName);

                    changeButtonsState(view, true);
                } else if (importStatus.getRowsProcessed() == 0) {
                    view.addMessage(L_BASIC_IMPORT_ERROR_FILE_EMPTY, ComponentState.MessageType.FAILURE);
                } else {
                    view.addMessage(L_BASIC_IMPORT_SUCCESS_MESSAGE, ComponentState.MessageType.SUCCESS, false,
                            String.valueOf(savedEntities));

                    importedCheckBox.setChecked(true);
                }
            } catch (Throwable throwable) {
                Throwables.propagateIfInstanceOf(throwable, FileNotFoundException.class);
                Throwables.propagateIfInstanceOf(throwable, IOException.class);

                view.addMessage(L_BASIC_IMPORT_ERROR_GENERIC, ComponentState.MessageType.FAILURE);

                if (LOG.isErrorEnabled()) {
                    LOG.error("An exception occurred while importing file!", throwable);
                }
            }
        }
    }

    public ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName) throws IOException {
        return importFile(fis, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, null, null);
    }

    public ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName, final Entity belongsTo,
            final String belongsToName) throws IOException {
        return importFile(fis, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, belongsTo, belongsToName, false,
                null, null, false);
    }

    public ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName, final Entity belongsTo,
            final String belongsToName, final Function<Entity, SearchCriterion> criteriaSupplier) throws IOException {
        return importFile(fis, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, belongsTo, belongsToName, true,
                criteriaSupplier, null, false);
    }

    public ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName,
            final Function<Entity, SearchCriterion> criteriaSupplier) throws IOException {
        return importFile(fis, cellBinderRegistry, rollbackOnError, pluginIdentifier, modelName, null, null, true,
                criteriaSupplier, null, false);
    }

    @Transactional
    public abstract ImportStatus importFile(final FileInputStream fis, final CellBinderRegistry cellBinderRegistry,
            final Boolean rollbackOnError, final String pluginIdentifier, final String modelName, final Entity belongsTo,
            final String belongsToName, final Boolean shouldUpdate, final Function<Entity, SearchCriterion> criteriaSupplier,
            final Function<Entity, Boolean> checkOnUpdate, final Boolean shouldSkip) throws IOException;

    public boolean shouldUpdate(final ViewDefinitionState view) {
        boolean shouldUpdate = false;

        CheckBoxComponent shouldUpdateCheckBox = (CheckBoxComponent) view.getComponentByReference(L_SHOULD_UPDATE);

        if (Objects.nonNull(shouldUpdateCheckBox)) {
            shouldUpdate = shouldUpdateCheckBox.isChecked();
        }

        return shouldUpdate;
    }

    private boolean shouldSkip(final ViewDefinitionState view) {
        boolean shouldSkip = false;

        CheckBoxComponent shouldSkipCheckBox = (CheckBoxComponent) view.getComponentByReference(L_SHOULD_SKIP);

        if (Objects.nonNull(shouldSkipCheckBox)) {
            shouldSkip = shouldSkipCheckBox.isChecked();
        }

        return shouldSkip;
    }

    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        return getDataDefinition(pluginIdentifier, modelName).create();
    }

    public Entity getEntity(final String pluginIdentifier, final String modelName, final SearchCriterion searchCriterion) {
        return getDataDefinition(pluginIdentifier, modelName).find().add(searchCriterion).setMaxResults(1).uniqueResult();
    }

    public void validateEntity(final Entity entity, final DataDefinition entityDD) {
    }

    public DataDefinition getDataDefinition(final String pluginIdentifier, final String modelName) {
        return dataDefinitionService.get(pluginIdentifier, modelName);
    }

    private void propagateErrors(final ImportStatus importStatus, final CellBinderRegistry cellBinderRegistry,
            final String filePath, final String pluginIdentifier, final String modelName) {
        Comparator<ImportError> compareByIndex = Comparator
                .comparing(error -> cellBinderRegistry.getIndexUsingFieldName(error.getFieldName()));
        Comparator<ImportError> comparator = Comparator.comparing(ImportError::getRowIndex).thenComparing(compareByIndex);

        importStatus.getErrors().stream().filter(error -> Objects.nonNull(error.getFieldName()))
                .filter(error -> Objects.nonNull(cellBinderRegistry.getIndexUsingFieldName(error.getFieldName())))
                .sorted(comparator).forEach(importError -> propagateErrors(importError, filePath, pluginIdentifier, modelName));
        importStatus.getErrors().stream().filter(error -> Objects.isNull(error.getFieldName()))
                .forEach(importError -> propagateErrors(importError, filePath, pluginIdentifier, modelName));
    }

    private void propagateErrors(final ImportError importError, final String filePath, final String pluginIdentifier,
            final String modelName) {
        String logType = getLogType(modelName);
        String action = translationService.translate(L_BASIC_IMPORT_ERROR_ACTION_SAVE, LocaleContextHolder.getLocale());

        String fileName = new StringBuilder(Files.getNameWithoutExtension(filePath)).append(L_DOT)
                .append(Files.getFileExtension(filePath)).toString();
        String rowIndex = String.valueOf(importError.getRowIndex() + 1);
        String relatedLine = translationService.translate(L_BASIC_IMPORT_ERROR_LINE_NUMBER, LocaleContextHolder.getLocale(),
                rowIndex);
        String fieldName = null;

        if (StringUtils.isNotEmpty(importError.getFieldName())) {
            fieldName = translationService.translate(createFieldNameKey(pluginIdentifier, modelName, importError.getFieldName()),
                    LocaleContextHolder.getLocale());
        }

        String message = translationService.translate(importError.getCode(), LocaleContextHolder.getLocale(),
                importError.getArgs());

        logService.add(LogService.Builder.error(logType, action).withMessage(message).withItem1(fileName).withItem2(relatedLine)
                .withItem3(fieldName));
    }

    public String getLogType(final String modelName) {
        return new StringBuilder(modelName).append(StringUtils.capitalize(L_IMPORT)).toString();
    }

    private String createFieldNameKey(final String pluginIdentifier, final String modelName, final String fieldName) {
        return new StringBuilder(pluginIdentifier).append(L_DOT).append(modelName).append(L_DOT).append(fieldName).append(L_DOT)
                .append(L_LABEL).toString();
    }

    public void redirectToLogs(final ViewDefinitionState view, final String modelName) {
        FieldComponent importFileField = (FieldComponent) view.getComponentByReference(L_IMPORT_FILE);
        String filePath = (String) importFileField.getFieldValue();

        if (StringUtils.isBlank(filePath)) {
            view.addMessage(L_BASIC_IMPORT_ERROR_FILE_REQUIRED, ComponentState.MessageType.FAILURE);
        } else if (!checkFileExtension(filePath)) {
            view.addMessage(L_BASIC_IMPORT_ERROR_FILE_INVALID, ComponentState.MessageType.FAILURE);
        } else {
            String url = UriComponentsBuilder.newInstance().path("basic").pathSegment("logsList.html").build().toUriString();

            String logType = getLogType(modelName);
            String fileName = new StringBuilder(Files.getNameWithoutExtension(filePath)).append(L_DOT)
                    .append(Files.getFileExtension(filePath)).toString();

            Map<String, String> filters = Maps.newHashMap();
            filters.put(LogFields.LOG_TYPE, logType);
            filters.put(LogFields.ITEM_1, fileName);

            Map<String, Object> gridOptions = Maps.newHashMap();
            gridOptions.put(L_FILTERS, filters);

            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put(L_GRID_OPTIONS, gridOptions);

            parameters.put(L_WINDOW_SHOW_BACK, true);

            view.openModal(url, parameters);
        }
    }

    public void changeButtonsState(final ViewDefinitionState view, final boolean isEnabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        Ribbon ribbon = window.getRibbon();

        RibbonGroup importRibbonGroup = ribbon.getGroupByName(L_IMPORT);

        RibbonActionItem redirectToLogsRibbonActionItem = importRibbonGroup.getItemByName(L_REDIRECT_TO_LOGS);

        redirectToLogsRibbonActionItem.setEnabled(isEnabled);
        redirectToLogsRibbonActionItem.requestUpdate(true);
    }

    public abstract boolean checkFileExtension(final String filePath);

}
