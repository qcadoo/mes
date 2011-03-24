/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.application;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.controller.ErrorController;
import com.qcadoo.model.api.CopyException;
import com.qcadoo.model.api.validators.ErrorMessage;

public final class TranslatedMessageExceptionResolver extends SimpleMappingExceptionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TranslatedMessageExceptionResolver.class);

    public static final String DEFAULT_EXCEPTION_MESSAGE_ATTRIBUTE = "exceptionMessage";

    private final Map<String, String> messageTranslations = new HashMap<String, String>();

    private final Map<Class<?>, String> exceptionTranslations = new HashMap<Class<?>, String>();

    @Autowired
    private ErrorController errorController;

    @Autowired
    private TranslationService translationService;

    @PostConstruct
    public void init() {
        exceptionTranslations.put(DataIntegrityViolationException.class, "dataIntegrityViolationException.objectInUse");
        exceptionTranslations.put(CopyException.class, "copyException");
        messageTranslations.put("Entity.* is in use", "dataIntegrityViolationException.objectInUse");
        messageTranslations.put("Entity.* cannot be found", "illegalStateException.entityNotFound");
        messageTranslations.put("PrintError:DocumentNotGenerated", "illegalStateException.printErrorDocumentNotGenerated");
    }

    @Override
    protected ModelAndView doResolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception exception) {
        ModelAndView mv = super.doResolveException(request, response, handler, exception);

        if (mv != null) {
            String codeStr = mv.getViewName();
            int code = Integer.parseInt(codeStr);

            CustomTranslationMessage customExceptionMessage = getCustomExceptionMessage(exception);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding exception message to view: " + customExceptionMessage);
            }

            String customExceptionMessageHeader = null;
            String customExceptionMessageExplanation = null;

            if (customExceptionMessage != null) {
                customExceptionMessageHeader = translationService.translate(
                        "core.errorPage.error." + customExceptionMessage.getMessage() + ".header",
                        LocaleContextHolder.getLocale());
                if (customExceptionMessage.getEntityIdentifier() != null) {
                    customExceptionMessageExplanation = translationService.translate("core.errorPage.error."
                            + customExceptionMessage.getMessage() + ".explanation", LocaleContextHolder.getLocale(),
                            customExceptionMessage.getEntityIdentifier());
                } else {
                    customExceptionMessageExplanation = translationService.translate("core.errorPage.error."
                            + customExceptionMessage.getMessage() + ".explanation", LocaleContextHolder.getLocale());
                }
                Throwable rootException = getRootException(exception);

                if (rootException instanceof CopyException) {
                    String copyExplanation = getAdditionalMessageForCopyException((CopyException) rootException,
                            LocaleContextHolder.getLocale());
                    if (copyExplanation != null) {
                        customExceptionMessageExplanation = copyExplanation;
                    }
                }
            }

            return errorController.getAccessDeniedPageView(code, exception, customExceptionMessageHeader,
                    customExceptionMessageExplanation, LocaleContextHolder.getLocale());
        }

        return mv;
    }

    private Throwable getRootException(final Throwable throwable) {
        if (throwable.getCause() != null) {
            return getRootException(throwable.getCause());
        } else if (throwable instanceof InvocationTargetException) {
            return getRootException(((InvocationTargetException) throwable).getTargetException());
        } else {
            return throwable;
        }
    }

    private String getAdditionalMessageForCopyException(final CopyException exception, final Locale locale) {
        for (Map.Entry<String, ErrorMessage> error : exception.getEntity().getErrors().entrySet()) {
            String field = translationService.translate(exception.getEntity().getPluginIdentifier() + "."
                    + exception.getEntity().getName() + "." + error.getKey() + ".label", locale);
            return field + " - " + translationService.translate(error.getValue().getMessage(), locale);
        }
        for (ErrorMessage error : exception.getEntity().getGlobalErrors()) {
            return translationService.translate(error.getMessage(), locale);
        }
        return null;
    }

    private CustomTranslationMessage getCustomExceptionMessage(final Throwable throwable) {
        CustomTranslationMessage exceptionMessage = getCustomExceptionMessageForClass(throwable);

        if (exceptionMessage != null) {
            return exceptionMessage;
        }

        String exceptionMessageString = throwable.getMessage();

        if (exceptionMessageString == null) {
            return null;
        }

        for (Map.Entry<String, String> translation : messageTranslations.entrySet()) {
            if (exceptionMessageString.matches(translation.getKey())) {
                String translationValue = translation.getValue();

                Pattern p = Pattern.compile("\\[ENTITY\\..+\\]");
                Matcher m = p.matcher(exceptionMessageString);
                if (m.find()) {
                    String entityIdentifier = exceptionMessageString.substring(m.start(0) + 8, m.end(0) - 1);
                    return new CustomTranslationMessage(translationValue, entityIdentifier);
                }

                return new CustomTranslationMessage(translationValue);
            }
        }

        return null;
    }

    private CustomTranslationMessage getCustomExceptionMessageForClass(final Throwable throwable) {
        for (Map.Entry<Class<?>, String> translation : exceptionTranslations.entrySet()) {
            if (translation.getKey().isInstance(throwable)) {
                return new CustomTranslationMessage(translation.getValue());
            }
        }

        if (throwable instanceof InvocationTargetException) {
            return getCustomExceptionMessage(((InvocationTargetException) throwable).getTargetException());
        } else if (throwable.getCause() != null) {
            return getCustomExceptionMessage(throwable.getCause());
        } else {
            return null;
        }

    }

    private class CustomTranslationMessage {

        private final String message;

        private final String entityIdentifier;

        public CustomTranslationMessage(final String message) {
            this(message, null);
        }

        public CustomTranslationMessage(final String message, final String entityIdentifier) {
            this.message = message;
            this.entityIdentifier = entityIdentifier;
        }

        public String getMessage() {
            return message;
        }

        public String getEntityIdentifier() {
            return entityIdentifier;
        }
    }
}
