/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.technologies.states;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.logging.TechnologyLoggingService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState.MessageType;

@Service
public class TechnologyStatesHook {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyStateChangeNotifierService technologyStateNotifier;

    @Autowired
    private TechnologyLoggingService technologyLoggingService;

    private static final String STATE_FIELD = "state";

    public final void onSave(final DataDefinition technologyDD, final Entity technology) {
        if (technology.getId() == null) {
            return;
        }

        Entity existingTechnology = technologyDD.get(technology.getId());
        if (existingTechnology == null) {
            return;
        }

        TechnologyState newState = TechnologyStateUtils.getStateFromField(technology.getStringField(STATE_FIELD));
        TechnologyState oldState = TechnologyStateUtils.getStateFromField(existingTechnology.getStringField(STATE_FIELD));

        List<MessageHolder> validationMessages = technologyStateNotifier.onTechnologyStateChange(existingTechnology, newState);
        assignValidationMessagesToEntity(technology, validationMessages);
        if (hasErrorMessages(validationMessages)) {
            technology.setField(STATE_FIELD, existingTechnology.getStringField(STATE_FIELD));
            return;
        }

        technologyLoggingService.logStateChange(technology, oldState, newState);
    }

    private boolean hasErrorMessages(final List<MessageHolder> validationMessages) {
        for (MessageHolder validationMessage : validationMessages) {
            if (validationMessage.getMessageType() == MessageType.FAILURE) {
                return true;
            }
        }
        return false;
    }

    private void assignValidationMessagesToEntity(final Entity technology, final List<MessageHolder> validationMessages) {
        DataDefinition technologyDD = technology.getDataDefinition();
        for (MessageHolder validationMessage : validationMessages) {
            if (validationMessage.getTargetReferenceName() == null) {
                technology.addGlobalError(translationService.translate(validationMessage.getMessageKey(), getLocale(),
                        validationMessage.getVars()));
                continue;
            }
            technology.addError(technologyDD.getField(validationMessage.getTargetReferenceName()),
                    validationMessage.getMessageKey());
        }
    }

}
