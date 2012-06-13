/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.orders.states;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DONE_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.TechnologyStateUtils;
import com.qcadoo.model.api.Entity;

@Service
public class OrderStateValidationService {

    private static final String ENTITY_IS_NULL = "entity is null";

    public List<ChangeOrderStateMessage> validationAccepted(final Entity entity) {
        checkArgument(entity != null, ENTITY_IS_NULL);
        List<String> references = Arrays.asList(DATE_TO, DATE_FROM, TECHNOLOGY);
        List<ChangeOrderStateMessage> message = checkValidation(references, entity);

        Entity technology = entity.getBelongsToField(TECHNOLOGY);
        if (technology == null) {
            return message;
        }
        TechnologyState technologyState = TechnologyStateUtils.getStateFromField(technology
                .getStringField(TechnologyFields.STATE));

        if (TechnologyState.ACCEPTED != technologyState) {
            message.add(ChangeOrderStateMessage.errorForComponent("orders.validate.technology.error.wrongState.accepted",
                    TECHNOLOGY));
        }
        return message;
    }

    public List<ChangeOrderStateMessage> validationInProgress(final Entity entity) {
        checkArgument(entity != null, ENTITY_IS_NULL);
        return validationAccepted(entity);
    }

    public List<ChangeOrderStateMessage> validationCompleted(final Entity entity) {
        checkArgument(entity != null, ENTITY_IS_NULL);
        List<String> references = Arrays.asList(DATE_TO, DATE_FROM, TECHNOLOGY, DONE_QUANTITY);
        return checkValidation(references, entity);
    }

    private List<ChangeOrderStateMessage> checkValidation(final List<String> references, final Entity entity) {
        checkArgument(entity != null, ENTITY_IS_NULL);
        List<ChangeOrderStateMessage> errors = Lists.newArrayList();
        for (String reference : references) {
            if (entity.getField(reference) == null) {
                errors.add(ChangeOrderStateMessage.errorForComponent("orders.order.orderStates.fieldRequired", reference));
            }
        }
        return errors;
    }
}
