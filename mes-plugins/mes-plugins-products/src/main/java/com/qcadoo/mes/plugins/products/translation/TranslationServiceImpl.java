package com.qcadoo.mes.plugins.products.translation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Controller;

import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldViewDefinition;
import com.qcadoo.mes.core.data.definition.FormDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.definition.ViewDefinition;
import com.qcadoo.mes.core.data.definition.ViewElementDefinition;
import com.qcadoo.mes.core.data.validation.ValidationError;
import com.qcadoo.mes.core.data.validation.ValidationResults;

@Controller
public class TranslationServiceImpl implements TranslationService {

    private static final String[] commonsMessages = new String[] { "commons.confirm.deleteMessage",
            "commons.loading.gridLoading", "commons.button.go", "commons.button.logout", "commons.form.button.accept",
            "commons.form.button.acceptAndClose", "commons.form.button.cancel", "commons.grid.button.new",
            "commons.grid.button.delete", "commons.grid.button.sort", "commons.grid.button.sort.asc",
            "commons.grid.button.sort.desc", "commons.grid.button.filter", "commons.grid.button.filter.null",
            "commons.grid.button.filter.notNull", "commons.grid.button.prev", "commons.grid.button.next",
            "commons.grid.span.pageInfo", "commons.grid.span.priority", "commons.grid.button.up", "commons.grid.button.down",
            "commons.validate.field.error.missing", "commons.validate.field.error.invalidNumericFormat",
            "commons.validate.field.error.invalidDateFormat", "commons.validate.field.error.invalidDateTimeFormat",
            "commons.validate.field.error.notMatch", "commons.validate.global.error", "commons.form.field.confirmable.label" };

    @Autowired
    private MessageSource messageSource;

    private String translateWithError(String messageCode, Locale locale) {
        return messageSource.getMessage(messageCode, null, locale);
    }

    public String translate(String messageCode, Locale locale) {
        return messageSource.getMessage(messageCode, null, "TO TRANSLATE: " + messageCode, locale);
    }

    public String translate(String messageCode, Object[] args, Locale locale) {
        return messageSource.getMessage(messageCode, args, "TO TRANSLATE: " + messageCode, locale);
    }

    public Map<String, String> getCommonsTranslations(Locale locale) {
        Map<String, String> commonsTranslations = new HashMap<String, String>();
        for (String commonMessage : commonsMessages) {
            commonsTranslations.put(commonMessage, translate(commonMessage, locale));
        }
        return commonsTranslations;
    }

    private void putTranslationToMap(String messageCode, Map<String, String> translationsMap, Locale locale) {
        translationsMap.put(messageCode, translate(messageCode, locale));
    }

    private void addGridColumnTranslation(ViewDefinition viewDefinition, GridDefinition gridDefinition, ColumnDefinition column,
            Map<String, String> translationsMap, Locale locale) {
        String messageCode = viewDefinition.getName() + "." + gridDefinition.getName() + ".column." + column.getName();
        try {
            translationsMap.put(messageCode, translateWithError(messageCode, locale));
        } catch (NoSuchMessageException e) {
            String entityFieldCode = "entity." + gridDefinition.getDataDefinition().getEntityName() + ".field."
                    + column.getFields().get(0).getName();
            translationsMap.put(messageCode, translate(entityFieldCode, locale));
        }
    }

    private void addFieldTranslation(ViewDefinition viewDefinition, ViewElementDefinition elementDefinition,
            FieldDefinition field, Map<String, String> translationsMap, Locale locale) {
        String messageCode = viewDefinition.getName() + "." + elementDefinition.getName() + ".field." + field.getName();
        try {
            translationsMap.put(messageCode, translateWithError(messageCode, locale));
        } catch (NoSuchMessageException e) {
            String entityFieldCode = "entity." + elementDefinition.getDataDefinition().getEntityName() + ".field."
                    + field.getName();
            translationsMap.put(messageCode, translate(entityFieldCode, locale));
        }
    }

    public void updateTranslationsForViewDefinition(ViewDefinition viewDefinition, Map<String, String> translationsMap,
            Locale locale) {
        if (viewDefinition.getHeader() != null) {
            putTranslationToMap(viewDefinition.getHeader(), translationsMap, locale);
        }
        for (ViewElementDefinition viewElement : viewDefinition.getElements()) {
            if (viewElement.getHeader() != null) {
                putTranslationToMap(viewElement.getHeader(), translationsMap, locale);
            }
            if (viewElement.getType() == ViewElementDefinition.TYPE_FORM) {
                FormDefinition formDefinition = (FormDefinition) viewElement;
                for (FieldViewDefinition fieldView : formDefinition.getFields()) {
                    addFieldTranslation(viewDefinition, formDefinition, fieldView.getDefinition(), translationsMap, locale);
                }

            } else if (viewElement.getType() == ViewElementDefinition.TYPE_GRID) {
                GridDefinition gridDefinition = (GridDefinition) viewElement;
                for (ColumnDefinition column : gridDefinition.getColumns()) {
                    addGridColumnTranslation(viewDefinition, gridDefinition, column, translationsMap, locale);
                }
                for (Entry<String, FieldDefinition> field : viewElement.getDataDefinition().getFields().entrySet()) {
                    addFieldTranslation(viewDefinition, gridDefinition, field.getValue(), translationsMap, locale);
                }
            }
        }
    }

    public void translateValidationResults(ValidationResults validationResults, Locale locale) {
        for (ValidationError error : validationResults.getGlobalErrors()) {
            error.setMessage(translate(error.getMessage(), error.getVars(), locale));
        }
        for (ValidationError error : validationResults.getErrors().values()) {
            error.setMessage(translate(error.getMessage(), error.getVars(), locale));
        }
    }
}
