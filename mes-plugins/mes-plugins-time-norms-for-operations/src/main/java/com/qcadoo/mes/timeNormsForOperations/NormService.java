/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.timeNormsForOperations;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;

@Service
public class NormService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyService technologyService;

    public List<String> checkOperationOutputQuantities(final Entity technology) {
        List<String> messages = new LinkedList<String>();

        List<Entity> operationComponents = technology.getTreeField("operationComponents");

        Locale locale = LocaleContextHolder.getLocale();

        for (Entity operationComponent : operationComponents) {
            BigDecimal timeNormsQuantity = getProductionInOneCycle(operationComponent);

            BigDecimal currentQuantity;

            try {
                currentQuantity = technologyService.getProductCountForOperationComponent(operationComponent);
            } catch (IllegalStateException e) {
                continue;
            }

            if (timeNormsQuantity.compareTo(currentQuantity) != 0) { // Not using equals intentionally
                StringBuilder message = new StringBuilder();
                message.append(translationService.translate("technologies.technology.validate.error.invalidQuantity1", locale));
                message.append(" ");
                message.append(operationComponent.getStringField("nodeNumber"));
                message.append(" ");
                message.append(translationService.translate("technologies.technology.validate.error.invalidQuantity2", locale));

                messages.add(message.toString());
            }
        }

        return messages;
    }

    private BigDecimal getProductionInOneCycle(final Entity operationComponent) {
        String entityType = operationComponent.getStringField("entityType");
        if ("operation".equals(entityType)) {
            return operationComponent.getDecimalField("productionInOneCycle");
        } else if ("referenceTechnology".equals(entityType)) {
            Entity refOperationComp = operationComponent.getBelongsToField("referenceTechnology")
                    .getTreeField("operationComponents").getRoot();
            return refOperationComp.getDecimalField("productionInOneCycle");
        } else {
            throw new IllegalStateException("operationComponent has illegal type, id = " + operationComponent.getId());
        }
    }
}
