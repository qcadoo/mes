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
package com.qcadoo.mes.materialFlowResources.hooks;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class DocumentsListHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        lockDispositionOrder(view);
    }

    private void lockDispositionOrder(ViewDefinitionState view) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference("grid");
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem dispositionOrderPdfItem = (RibbonActionItem) window.getRibbon().getGroupByName("print").getItemByName("printDispositionOrderPdf");
        dispositionOrderPdfItem.setEnabled(false);

        List<String> documentTypesWithDispositionOrder = Arrays.asList(DocumentType.TRANSFER.getStringValue(), DocumentType.INTERNAL_OUTBOUND.getStringValue(), DocumentType.RELEASE.getStringValue());

        String errorMessage = null;
        for (Entity document : gridComponent.getSelectedEntities()) {
            String documentType = document.getStringField(DocumentFields.TYPE);
            if (documentType == null || !documentTypesWithDispositionOrder.contains(documentType)) {
                errorMessage = "materialFlowResources.printDispositionOrderPdf.error";
                break;
            }

        }

        dispositionOrderPdfItem.setMessage(errorMessage);
        dispositionOrderPdfItem.setEnabled(!gridComponent.getSelectedEntities().isEmpty() && errorMessage == null);
        dispositionOrderPdfItem.requestUpdate(true);
    }
}
