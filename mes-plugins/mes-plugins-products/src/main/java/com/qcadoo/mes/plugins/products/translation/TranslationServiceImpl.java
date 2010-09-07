package com.qcadoo.mes.plugins.products.translation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.definition.ViewDefinition;
import com.qcadoo.mes.core.data.definition.ViewElementDefinition;

@Controller
public class TranslationServiceImpl implements TranslationService {

    private static final String[] commonsMessages = new String[] { "commons.confirm.deleteMessage",
            "commons.loading.gridLoading", "commons.button.go", "commons.button.logout", "commons.form.button.accept",
            "commons.form.button.acceptAndClose", "commons.form.button.cancel", "commons.grid.button.new",
            "commons.grid.button.delete", "commons.grid.button.sort", "commons.grid.button.sort.asc",
            "commons.grid.button.sort.desc", "commons.grid.button.filter", "commons.grid.button.filter.null",
            "commons.grid.button.filter.notNull", "commons.grid.button.prev", "commons.grid.button.next",
            "commons.grid.span.pageInfo", "commons.grid.span.priority", "commons.grid.button.up", "commons.grid.button.down" };

    @Autowired
    private MessageSource messageSource;

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

    public void translateViewDefinition(ViewDefinition viewDefinition, Locale locale) {
        viewDefinition.setHeader(translate(viewDefinition.getHeader(), locale));
        for (ViewElementDefinition viewElement : viewDefinition.getElements()) {
            if (viewElement.getHeader() != null) {
                viewElement.setHeader(translate(viewElement.getHeader(), locale));
            }
            if (viewElement.getType() == ViewElementDefinition.TYPE_GRID) {
                GridDefinition gridDefinition = (GridDefinition) viewElement;
                for (ColumnDefinition column : gridDefinition.getColumns()) {
                    column.setName(translate(column.getName(), locale));
                }

            }
        }
    }
}
