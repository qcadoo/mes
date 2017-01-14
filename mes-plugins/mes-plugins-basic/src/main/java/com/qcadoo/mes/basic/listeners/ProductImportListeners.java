/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.listeners;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.product.importing.ImportError;
import com.qcadoo.mes.basic.product.importing.ImportStatus;
import com.qcadoo.mes.basic.product.importing.XlsxImportService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;

import static com.qcadoo.mes.basic.product.importing.SpreadsheetSchemaInfo.getIndexUsingFieldName;

@Service
public class ProductImportListeners {

    private static final Logger LOG = LoggerFactory.getLogger(ProductImportListeners.class);

    private final XlsxImportService xlsxImportService;

    private final TranslationService translationService;

    @Autowired
    public ProductImportListeners(XlsxImportService xlsxImportService, TranslationService translationService) {
        this.xlsxImportService = xlsxImportService;
        this.translationService = translationService;
    }

    public void navigateToProductImportPage(final ViewDefinitionState view, final ComponentState state,
                                            final String[] args) {
        view.redirectTo("/page/basic/productsImport.html", false, true);
    }

    public void navigateToProductImportSchema(final ViewDefinitionState view, final ComponentState state,
                                              final String[] args) {
        String redirectUrl = new StringBuilder("/basic/resources/")
                .append("productImportSchema_")
                .append(LocaleContextHolder.getLocale().getLanguage())
                .append(".xlsx")
                .toString();
        view.redirectTo(redirectUrl, true, false);
    }

    private ErrorMessage translatedErrorMessage(final String code) {
        return new ErrorMessage(translate(code));
    }

    private String translate(String messageCode, String... args) {
        return translationService.translate(messageCode, LocaleContextHolder.getLocale(), args);
    }

    public void uploadProductImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args) throws IOException {
        Object fieldValue = state.getFieldValue();
        String filePath = fieldValue.toString();
        if (StringUtils.isBlank(filePath)) {
            state.addMessage(translatedErrorMessage("basic.productsImport.error.file.required"));
        } else if (!Files.getFileExtension(filePath).equalsIgnoreCase("xlsx")) {
            state.addMessage(translatedErrorMessage("basic.productsImport.error.file.invalid"));
        } else {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                final ImportStatus importStatus = xlsxImportService.importFrom(new XSSFWorkbook(fis));
                if (importStatus.hasErrors()) {
                    // TODO Find out how to present more detailed error messages to the user
                    prepareMessages(importStatus, view);
                } else if (0 == importStatus.getRowsProcessed()) {
                    view.addMessage(translatedErrorMessage("basic.productsImport.error.file.empty"));
                } else {
                    view.addMessage("basic.productsImport.success.message",
                            ComponentState.MessageType.SUCCESS,
                            false,
                            String.valueOf(importStatus.getRowsProcessed()));
                }
            } catch (Throwable throwable) {
                // There is not much we can do about these IO exceptions except rethrowing them
                Throwables.propagateIfInstanceOf(throwable, FileNotFoundException.class);
                Throwables.propagateIfInstanceOf(throwable, IOException.class);

                view.addMessage(translatedErrorMessage("basic.productsImport.error.generic"));

                if (LOG.isErrorEnabled()) {
                    LOG.error("An exception occured while importing products", throwable);
                }
            }
        }
    }

    private void prepareMessages(ImportStatus importStatus, ViewDefinitionState view) {
        Comparator<ImportError> compareByIndex = (o1, o2) ->
                Integer.valueOf(getIndexUsingFieldName(o1.getFieldName()))
                        .compareTo(getIndexUsingFieldName(o2.getFieldName()));

        Comparator<ImportError> comparator =
                Comparator.comparing(ImportError::getRowIndex)
                        .thenComparing(compareByIndex);

        importStatus.getErrors()
                .stream()
                .sorted(comparator)
                .forEach(importError -> {
                    String locationString = translate(
                            "basic.productsImport.error.message",
                            String.valueOf(importError.getRowIndex()),
                            String.valueOf(getIndexUsingFieldName(importError.getFieldName()) + 1));

                    String errorMessage = locationString + " " + translate(importError.getCode(), importError.getArgs());
                    view.addTranslatedMessage(errorMessage, ComponentState.MessageType.FAILURE, false);
                });
    }
}
