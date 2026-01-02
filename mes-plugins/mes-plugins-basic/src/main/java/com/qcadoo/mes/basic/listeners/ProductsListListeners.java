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


import com.google.common.collect.Lists;
import com.lowagie.text.pdf.Barcode128;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductsListListeners {

    public void openProductsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/basic/productsImport.html");

        view.openModal(url.toString());
    }

    public void openEansImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/basic/eansImport.html");

        view.openModal(url.toString());
    }

    public void printProductLabels(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> ids = grid.getSelectedEntitiesIds();

        if (ids.isEmpty()) {
            view.addMessage("basic.productsList.error.notSelected", ComponentState.MessageType.INFO);
        } else {
            List<String> invalidNumbers = Lists.newArrayList();

            grid.getSelectedEntities().forEach(product -> {
                String code = product.getStringField(ProductFields.EAN);
                if (code == null) {
                    code = product.getStringField(ProductFields.NUMBER);
                }

                try {
                    Barcode128.getRawText(code, false);
                } catch (RuntimeException exception) {
                    invalidNumbers.add(code);
                }
            });

            if (invalidNumbers.isEmpty()) {
                String redirectUrl = new StringBuilder("/basic/productLabelsReport.pdf?")
                        .append(ids.stream().map(id -> "ids=" + id.toString()).collect(Collectors.joining("&")))
                        .toString();

                view.redirectTo(redirectUrl, true, false);
            } else {
                view.addMessage("basic.product.productLabelsReport.number.invalidCharacters", ComponentState.MessageType.FAILURE, String.join(", ", invalidNumbers));
            }
        }
    }
}
