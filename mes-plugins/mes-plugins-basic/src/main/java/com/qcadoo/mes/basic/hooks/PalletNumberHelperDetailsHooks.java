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
package com.qcadoo.mes.basic.hooks;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.PalletNumbersService;
import com.qcadoo.mes.basic.constants.PalletNumberHelperFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class PalletNumberHelperDetailsHooks {

    public static final String L_FORM = "form";

    public static final String L_WINDOW = "window";

    public static final String L_ACTIONS = "actions";

    public static final String L_PRINT = "print";

    public static final String L_SAVE = "save";

    public static final String L_PRINT_PALLET_NUMBER_HELPER_REPORT = "printPalletNumberHelperReport";

    @Autowired
    private PalletNumbersService palletNumbersService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillPalletNumbers(view);

        disableFields(view);

        disableButtonsWhenNotSaved(view);
    }

    private void fillPalletNumbers(final ViewDefinitionState view) {
        FormComponent palletNumberHelperForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent numbersField = (FieldComponent) view.getComponentByReference(PalletNumberHelperFields.NUMBERS);

        Long palletNumberHelperId = palletNumberHelperForm.getEntityId();

        boolean isSaved = (palletNumberHelperId != null);

        if (isSaved) {
            Entity palletNumberHelper = palletNumbersService.getPalletNumberHelper(palletNumberHelperId);

            List<Entity> palletNumbers = palletNumberHelper.getManyToManyField(PalletNumberHelperFields.PALLET_NUMBERS);

            numbersField.setFieldValue(buildNumbersString(palletNumbers));
            numbersField.requestComponentUpdateState();
        }
    }

    private String buildNumbersString(final List<Entity> palletNumbers) {
        StringBuilder numbersString = new StringBuilder();

        if (!palletNumbers.isEmpty()) {
            List<String> numbers = palletNumbersService.getNumbers(palletNumbers);

            int i = 0;

            for (String number : numbers) {
                numbersString.append(number);

                if (i != (palletNumbers.size() - 1)) {
                    numbersString.append("\n");
                }

                i++;
            }
        }
        return numbersString.toString();
    }

    private void disableFields(final ViewDefinitionState view) {
        FormComponent palletNumberHelperForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(PalletNumberHelperFields.QUANTITY);

        Long palletNumberHelperId = palletNumberHelperForm.getEntityId();

        boolean isEnabled = (palletNumberHelperId == null);

        quantityField.setEnabled(isEnabled);
        quantityField.requestComponentUpdateState();
    }

    private void disableButtonsWhenNotSaved(final ViewDefinitionState view) {
        FormComponent palletNumberHelperForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup actionsRibbonGroup = ribbon.getGroupByName(L_ACTIONS);
        RibbonGroup printRibbonGroup = ribbon.getGroupByName(L_PRINT);

        RibbonActionItem saveRibbonActionItem = actionsRibbonGroup.getItemByName(L_SAVE);
        RibbonActionItem printPalletNumberReportHelperRibbonActionItem = printRibbonGroup
                .getItemByName(L_PRINT_PALLET_NUMBER_HELPER_REPORT);

        Long palletNumberHelperId = palletNumberHelperForm.getEntityId();

        boolean isEnabled = (palletNumberHelperId != null);

        if (saveRibbonActionItem != null) {
            saveRibbonActionItem.setEnabled(!isEnabled);

            saveRibbonActionItem.requestUpdate(true);
        }

        if (printPalletNumberReportHelperRibbonActionItem != null) {
            printPalletNumberReportHelperRibbonActionItem.setEnabled(isEnabled);

            printPalletNumberReportHelperRibbonActionItem.requestUpdate(true);
        }
    }

}
