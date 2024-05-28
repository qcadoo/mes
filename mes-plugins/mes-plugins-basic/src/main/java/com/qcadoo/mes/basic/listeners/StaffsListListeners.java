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
package com.qcadoo.mes.basic.listeners;

import com.beust.jcommander.internal.Lists;
import com.lowagie.text.pdf.Barcode128;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StaffsListListeners {

    public void openStaffsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/basic/staffsImport.html");

        view.openModal(url.toString());
    }

    public void printStaffLabels(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent staffsGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> staffIds = staffsGrid.getSelectedEntitiesIds();

        if (staffIds.isEmpty()) {
            view.addMessage("basic.staffsList.error.notSelected", ComponentState.MessageType.INFO);
        } else {
            List<String> invalidNumbers = Lists.newArrayList();

            staffsGrid.getSelectedEntities().forEach(staff -> {
                String number = staff.getStringField(StaffFields.NUMBER);

                try {
                    Barcode128.getRawText(number, false);
                } catch (RuntimeException exception) {
                    invalidNumbers.add(number);
                }
            });

            if (invalidNumbers.isEmpty()) {
                String redirectUrl = new StringBuilder("/basic/staffLabelsReport.pdf?")
                        .append(staffIds.stream().map(staffId -> "ids=" + staffId.toString()).collect(Collectors.joining("&")))
                        .toString();

                view.redirectTo(redirectUrl, true, false);
            } else {
                view.addMessage("basic.staff.staffLabelsReport.number.invalidCharacters", ComponentState.MessageType.FAILURE, String.join(", ", invalidNumbers));
            }
        }
    }

}
