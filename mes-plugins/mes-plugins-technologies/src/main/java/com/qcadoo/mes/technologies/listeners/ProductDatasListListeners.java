/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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

import com.qcadoo.mes.technologies.constants.ProductDataFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class ProductDatasListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ReportService reportService;

    public void printReport(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        GridComponent productDatasGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        productDatasGrid.getSelectedEntitiesIds().forEach(productDataId -> {
            Entity productData = getProductDataDD().get(productDataId);

            if (Objects.isNull(productData)) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
            } else if (StringUtils.hasText(productData.getStringField(ProductDataFields.FILE_NAME))) {
                StringBuilder urlBuilder = new StringBuilder();

                urlBuilder.append("/generateSavedReport/").append(TechnologiesConstants.PLUGIN_IDENTIFIER);
                urlBuilder.append("/").append(TechnologiesConstants.MODEL_PRODUCT_DATA).append(".");
                urlBuilder.append(args[0]).append("?id=").append(productDataId);

                view.redirectTo(urlBuilder.toString(), true, false);
            } else {
                state.addMessage("qcadooReport.errorMessage.documentsWasNotGenerated", MessageType.FAILURE);
            }
        });
    }

    private DataDefinition getProductDataDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_DATA);
    }

}
