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
package com.qcadoo.mes.productCharacteristics.aop;

import java.util.Locale;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productCharacteristics.constants.ProductCharacteristicsFields;
import com.qcadoo.mes.productCharacteristics.constants.ProductFieldsPC;
import com.qcadoo.mes.productCharacteristics.constants.ShelvesFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(ProductCharacteristicsFields.PLUGIN_IDENTIFIER)
public class WorkPlanPdfForDivisionAspectPC {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Pointcut("execution(private java.lang.String com.qcadoo.mes.workPlans.pdf.document.WorkPlanPdfForDivision.prepareMainOrderSummary(..)) "
            + "&& args(order, product, locale)")
    public void prepareMainOrderSummary(Entity order, Entity product, Locale locale) {
    }

    @Around("prepareMainOrderSummary(order ,product, locale)")
    public String aroundPrepareMainOrderSummary(final ProceedingJoinPoint pjp, Entity order, Entity product, Locale locale)
            throws Throwable {

        String baseSummary = prepareBaseSummary(order, product, locale);
        Entity upForm = product.getBelongsToField(ProductFieldsPC.UP_FORM);
        Entity downForm = product.getBelongsToField(ProductFieldsPC.DOWN_FORM);
        Entity upShelf = product.getBelongsToField(ProductFieldsPC.UP_SHELVE);
        Entity downShelf = product.getBelongsToField(ProductFieldsPC.DOWN_SHELVE);

        StringBuilder newSummary = new StringBuilder(baseSummary);
        if (downShelf != null) {
            newSummary.append(", ");
            newSummary.append(downShelf.getStringField(ShelvesFields.NUMBER));

            if (upShelf != null && !upShelf.getId().equals(downShelf.getId())) {

                newSummary.append(", ");
                newSummary.append(upShelf.getStringField(ShelvesFields.NUMBER));
            }
        } else {
            if (upShelf != null) {
                newSummary.append(", ");
                newSummary.append(upShelf.getStringField(ShelvesFields.NUMBER));
            }
        }
        if (downForm != null) {
            newSummary.append(", ");
            newSummary.append(downForm.getStringField(ShelvesFields.NUMBER));

            if (upForm != null && !upForm.getId().equals(downForm.getId())) {

                newSummary.append(", ");
                newSummary.append(upForm.getStringField(ShelvesFields.NUMBER));
            }
        } else {
            if (upForm != null) {
                newSummary.append(", ");
                newSummary.append(upForm.getStringField(ShelvesFields.NUMBER));
            }
        }
        return newSummary.toString();
    }

    private String prepareBaseSummary(Entity order, Entity product, Locale locale) {

        StringBuilder summary = new StringBuilder(translationService.translate("workPlans.workPlan.report.mainOrder", locale));
        summary.append(" ");
        summary.append(order.getStringField("number"));
        summary.append(", ");
        summary.append(product.getStringField(ProductFields.NAME));
        summary.append(", ");
        summary.append(numberService.formatWithMinimumFractionDigits(order.getDecimalField("plannedQuantity"), 0));
        summary.append(" ");
        summary.append(product.getStringField(ProductFields.UNIT));
        return summary.toString();
    }
}
