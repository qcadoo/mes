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
import com.qcadoo.mes.basic.product.importing.ImportException;
import com.qcadoo.mes.basic.product.importing.ImportStatus;
import com.qcadoo.mes.basic.product.importing.XlsImportSevice;
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

@Service
public class ProductImportListeners {

    private static final Logger LOG = LoggerFactory.getLogger(ProductImportListeners.class);

    private final XlsImportSevice xlsImportSevice;

    private final TranslationService translationService;

    @Autowired
    public ProductImportListeners(XlsImportSevice xlsImportSevice, TranslationService translationService) {
        this.xlsImportSevice = xlsImportSevice;
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

    public void uploadProductImportFile(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) throws IOException {
        Object fieldValue = state.getFieldValue();
        String filePath = fieldValue.toString();
        if (StringUtils.isBlank(filePath)) {
            state.addMessage(new ErrorMessage(
                    translationService.translate("basic.productsImport.error.file.required",
                            LocaleContextHolder.getLocale()
                    )
            ));
        } else if (!Files.getFileExtension(filePath).equalsIgnoreCase("xlsx")) {
            state.addMessage(new ErrorMessage(
                    translationService.translate("basic.productsImport.error.file.invalid",
                            LocaleContextHolder.getLocale()
                    )
            ));
        } else {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                final ImportStatus importStatus = xlsImportSevice.importFrom(new XSSFWorkbook(fis));
                if (importStatus.hasErrors()) {
                    // TODO Find out how to present more detailed error messages to the user
                    state.addMessage(new ErrorMessage("Błąd podczas eksportu"));
                } else {
                    // TODO It may be wise to inform the user about how many records got created
                    view.redirectTo("/page/basic/productsList.html", false, false);
                }
            } catch (ImportException ie) {
                ie.printStackTrace();
            } catch (Throwable throwable) {
                // There is not much we can do about these IO exceptions except rethrowing them
                Throwables.propagateIfInstanceOf(throwable, FileNotFoundException.class);
                Throwables.propagateIfInstanceOf(throwable, IOException.class);

                if (LOG.isErrorEnabled()) {
                    LOG.error("An exception occured while importing products", throwable);
                }
            }
        }
    }
}
