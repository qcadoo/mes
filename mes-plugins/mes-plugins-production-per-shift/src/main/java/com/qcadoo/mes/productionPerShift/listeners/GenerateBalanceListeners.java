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
package com.qcadoo.mes.productionPerShift.listeners;

import com.qcadoo.mes.productionPerShift.BalanceGenerationStrategy;
import com.qcadoo.mes.productionPerShift.ProductionBalancePerShiftGenerator;
import com.qcadoo.mes.productionPerShift.constants.BalanceContextFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class GenerateBalanceListeners {

    @Autowired
    private ProductionBalancePerShiftGenerator productionBalancePerShiftGenerator;

    public void generateBalance(final ViewDefinitionState viewState, final ComponentState formState, final String[] args) {
        FormComponent form = (FormComponent) formState;
        Entity contextEntity = form.getPersistedEntityWithIncludedFormValues();
        // We don't want to reuse contexts - in case of the user working with many browser tabs to compare a couple of results
        contextEntity.setId(null);
        contextEntity.setField(BalanceContextFields.BALANCES, Collections.<Entity> emptyList());

        // Call validation
        contextEntity = contextEntity.getDataDefinition().save(contextEntity);
        if (!contextEntity.isValid()) {
            form.setEntity(contextEntity);
            return;
        }
        BalanceGenerationStrategy strategy = resolveBalanceGenerationStrategy(contextEntity);
        List<Entity> balances = productionBalancePerShiftGenerator.generate(strategy);
        contextEntity.setField(BalanceContextFields.BALANCES, balances);

        Entity persistedContext = contextEntity.getDataDefinition().save(contextEntity);
        if (persistedContext.isValid()) {
            // Show 'balances' tab
            WindowComponent window = (WindowComponent) viewState.getComponentByReference(QcadooViewConstants.L_WINDOW);
            window.setActiveTab("balances");
        }
        form.setEntity(persistedContext);
    }

    private BalanceGenerationStrategy resolveBalanceGenerationStrategy(final Entity contextEntity) {
        boolean plannedRequired = contextEntity.getBooleanField(BalanceContextFields.PLANNED_QUANTITY_REQUIRED);
        boolean deviationRequired = contextEntity.getBooleanField(BalanceContextFields.DEVIATION_REQUIRED);
        BigDecimal deviationThreshold = contextEntity.getDecimalField(BalanceContextFields.DEVIATION_THRESHOLD);

        return BalanceGenerationStrategy.forInterval(extractSearchInterval(contextEntity))
                .withPlannedQuantityRequired(plannedRequired).withDeviationRequired(deviationRequired)
                .withDeviationThreshold(deviationThreshold);
    }

    private Interval extractSearchInterval(final Entity contextEntity) {
        Date from = contextEntity.getDateField(BalanceContextFields.FROM_DATE);
        Date to = contextEntity.getDateField(BalanceContextFields.TO_DATE);
        DateTime toMidnight = new DateTime(to).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
        return new Interval(new DateTime(from), toMidnight);
    }

    public void onFilterCheckboxChange(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        CheckBoxComponent plannedRequiredCheckbox = (CheckBoxComponent) view.getComponentByReference("plannedQuantityRequired");
        CheckBoxComponent deviationRequiredCheckbox = (CheckBoxComponent) view.getComponentByReference("deviationRequired");
        FieldComponent deviationThreshold = (FieldComponent) view.getComponentByReference("deviationThreshold");

        if (plannedRequiredCheckbox == null || deviationRequiredCheckbox == null) {
            return;
        }
        deviationRequiredCheckbox.setEnabled(plannedRequiredCheckbox.isChecked());
        if (!plannedRequiredCheckbox.isChecked()) {
            deviationRequiredCheckbox.setChecked(false);
        }
        boolean thresholdEnabled = plannedRequiredCheckbox.isChecked() && deviationRequiredCheckbox.isChecked();
        deviationThreshold.setEnabled(thresholdEnabled);
        deviationThreshold.setRequired(thresholdEnabled);
        if (!(plannedRequiredCheckbox.isChecked() && deviationRequiredCheckbox.isChecked())) {
            deviationThreshold.setFieldValue(null);
        }
    }

}
