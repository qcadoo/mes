package com.qcadoo.mes.technologies.states;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

        TechnologyState newState = getTechnologyStateFromString(technology.getStringField(STATE_FIELD));
        TechnologyState oldState = getTechnologyStateFromString(existingTechnology.getStringField(STATE_FIELD));

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
            technology.addError(technologyDD.getField(validationMessage.getTargetReferenceName()), translationService.translate(
                    validationMessage.getMessageKey(), validationMessage.getTargetReferenceName(), getLocale(),
                    validationMessage.getVars()));
        }
    }

    private TechnologyState getTechnologyStateFromString(final String stringValue) {
        if (!StringUtils.hasText(stringValue)) {
            return null;
        }
        return TechnologyState.valueOf(stringValue.toUpperCase(Locale.getDefault()));
    }
}
