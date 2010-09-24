package com.qcadoo.mes.core.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Controller;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.api.TranslationService;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.validation.ErrorMessage;
import com.qcadoo.mes.core.view.ViewDefinition;

@Controller
public final class TranslationServiceImpl implements TranslationService {

    private static final String[] COMMONS_MESSAGES = new String[] { "commons.confirm.deleteMessage",
            "commons.loading.gridLoading", "commons.button.go", "commons.button.logout", "commons.form.button.accept",
            "commons.form.button.acceptAndClose", "commons.form.button.cancel", "commons.form.message.save",
            "commons.grid.button.new", "commons.grid.button.delete", "commons.grid.button.sort", "commons.grid.button.sort.asc",
            "commons.grid.button.sort.desc", "commons.grid.button.filter", "commons.grid.button.filter.null",
            "commons.grid.button.filter.notNull", "commons.grid.button.prev", "commons.grid.button.next",
            "commons.grid.span.pageInfo", "commons.grid.span.priority", "commons.grid.button.up", "commons.grid.button.down",
            "commons.validate.field.error.missing", "commons.validate.field.error.invalidNumericFormat",
            "commons.validate.field.error.invalidDateFormat", "commons.validate.field.error.invalidDateTimeFormat",
            "commons.validate.field.error.notMatch", "commons.validate.global.error", "commons.form.field.confirmable.label" };

    private static final String[] LOGIN_MESSAGES = new String[] { "login.form.label.language", "login.form.label.login",
            "login.form.label.password", "login.form.button.logIn", "login.message.error", "login.message.logout",
            "login.message.timeout", "login.message.accessDenied.header", "login.message.accessDenied.info" };

    @Autowired
    private MessageSource messageSource;

    @Override
    public String translate(final String messageCode, final Locale locale) {
        return messageSource.getMessage(messageCode, null, "TO TRANSLATE: " + messageCode, locale);
    }

    @Override
    public String translate(final String messageCode, final Object[] args, final Locale locale) {
        return messageSource.getMessage(messageCode, args, "TO TRANSLATE: " + messageCode, locale);
    }

    @Override
    public String translate(final List<String> messageCodes, final Locale locale) {
        for (String messageCode : messageCodes) {
            try {
                return translateWithError(messageCode, locale);
            } catch (NoSuchMessageException e) {

            }
        }
        return "TO TRANSLATE: " + messageCodes;
    }

    @Override
    public String translate(final List<String> messageCodes, final Object[] args, final Locale locale) {
        for (String messageCode : messageCodes) {
            try {
                return translateWithError(messageCode, args, locale);
            } catch (NoSuchMessageException e) {

            }
        }
        return "TO TRANSLATE: " + messageCodes;
    }

    private String translateWithError(final String messageCode, final Locale locale) {
        return messageSource.getMessage(messageCode, null, locale);
    }

    private String translateWithError(final String messageCode, final Object[] args, final Locale locale) {
        return messageSource.getMessage(messageCode, args, locale);
    }

    @Override
    public Map<String, String> getCommonsTranslations(final Locale locale) {
        Map<String, String> commonsTranslations = new HashMap<String, String>();
        for (String commonMessage : COMMONS_MESSAGES) {
            commonsTranslations.put(commonMessage, translate(commonMessage, locale));
        }
        return commonsTranslations;
    }

    @Override
    public Map<String, String> getLoginTranslations(final Locale locale) {
        Map<String, String> loginTranslations = new HashMap<String, String>();
        for (String loginMessage : LOGIN_MESSAGES) {
            loginTranslations.put(loginMessage, translate(loginMessage, locale));
        }
        return loginTranslations;
    }

    @Override
    public String getEntityFieldMessageCode(final DataDefinition dataDefinition, final String fieldName) {
        return dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "." + fieldName + ".label";
    }

    // private void putTranslationToMap(final String messageCode, final Map<String, String> translationsMap, final Locale locale)
    // {
    // translationsMap.put(messageCode, translate(messageCode, locale));
    // }
    //
    // private void addGridColumnTranslation(final ViewDefinition viewDefinition, final GridComponent gridDefinition,
    // final ColumnDefinition column, final Map<String, String> translationsMap, final Locale locale) {
    // String messageCode = viewDefinition.getName() + "." + gridDefinition.getName() + ".column." + column.getName();
    // try {
    // translationsMap.put(messageCode, translateWithError(messageCode, locale));
    // } catch (NoSuchMessageException e) {
    // String entityFieldCode = "entity." + gridDefinition.getDataDefinition().getName() + ".field."
    // + column.getFields().get(0).getName();
    // translationsMap.put(messageCode, translate(entityFieldCode, locale));
    // }
    // }

    // private void addDataFieldTranslation(final ViewDefinition viewDefinition, final ComponentDefinition elementDefinition,
    // final DataFieldDefinition field, final Map<String, String> translationsMap, final Locale locale) {
    // String messageCode = viewDefinition.getName() + "." + elementDefinition.getName() + ".field." + field.getName();
    // try {
    // translationsMap.put(messageCode, translateWithError(messageCode, locale));
    // } catch (NoSuchMessageException e) {
    // String entityFieldCode = "entity." + elementDefinition.getDataDefinition().getName() + ".field." + field.getName();
    // translationsMap.put(messageCode, translate(entityFieldCode, locale));
    // }
    // }

    // private void addFormFieldTranslation(final ViewDefinition viewDefinition, final ComponentDefinition elementDefinition,
    // final FormFieldDefinition field, final Map<String, String> translationsMap, final Locale locale) {
    // String messageCode = viewDefinition.getName() + "." + elementDefinition.getName() + ".field." + field.getName();
    // try {
    // translationsMap.put(messageCode, translateWithError(messageCode, locale));
    // } catch (NoSuchMessageException e) {
    // String entityFieldCode = "entity." + elementDefinition.getDataDefinition().getName() + ".field." + field.getName();
    // translationsMap.put(messageCode, translate(entityFieldCode, locale));
    // }
    // }

    public void updateTranslationsForViewDefinition(final ViewDefinition viewDefinition,
            final Map<String, String> translationsMap, final Locale locale) {
        // if (viewDefinition.getHeader() != null) {
        // putTranslationToMap(viewDefinition.getHeader(), translationsMap, locale);
        // }
        // for (ComponentDefinition viewElement : viewDefinition.getElements()) {
        // if (viewElement.getHeader() != null) {
        // putTranslationToMap(viewElement.getHeader(), translationsMap, locale);
        // }
        // if (viewElement.getType() == ComponentDefinition.TYPE_CONTAINER_FORM) {
        // FormDefinition formDefinition = (FormDefinition) viewElement;
        // for (FormFieldDefinition formField : formDefinition.getFields()) {
        // addFormFieldTranslation(viewDefinition, formDefinition, formField, translationsMap, locale);
        // }
        //
        // } else if (viewElement.getType() == ComponentDefinition.TYPE_CONTAINER_GRID) {
        // GridDefinition gridDefinition = (GridDefinition) viewElement;
        // for (ColumnDefinition column : gridDefinition.getColumns()) {
        // addGridColumnTranslation(viewDefinition, gridDefinition, column, translationsMap, locale);
        // }
        // for (Entry<String, DataFieldDefinition> field : viewElement.getDataDefinition().getFields().entrySet()) {
        // addDataFieldTranslation(viewDefinition, gridDefinition, field.getValue(), translationsMap, locale);
        // }
        // }
        // }
    }

    public void translateEntity(final Entity entity, final Locale locale) {
        for (ErrorMessage error : entity.getGlobalErrors()) {
            error.setMessage(translate(error.getMessage(), error.getVars(), locale));
        }
        for (ErrorMessage error : entity.getErrors().values()) {
            error.setMessage(translate(error.getMessage(), error.getVars(), locale));
        }
    }
}
