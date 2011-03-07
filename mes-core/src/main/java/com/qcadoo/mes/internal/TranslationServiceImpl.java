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

package com.qcadoo.mes.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public final class TranslationServiceImpl implements TranslationService, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(TranslationServiceImpl.class);

    private static final Logger TRANSLATION_LOG = LoggerFactory.getLogger("TRANSLATION");

    private static final Set<String> COMMONS_MESSAGES = new HashSet<String>();

    private static final Set<String> SECURITY_MESSAGES = new HashSet<String>();

    private static final Set<String> DASHBOARD_MESSAGES = new HashSet<String>();

    private static final String DEFAULT_MISSING_MESSAGE = "-";

    @Value("${ignoreMissingTranslations}")
    private boolean ignoreMissingTranslations;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public String translate(final String messageCode, final Locale locale, final Object... args) {
        String message = translateWithError(messageCode, locale, args);
        if (message != null) {
            return message.trim();
        }

        TRANSLATION_LOG.warn("Missing translation " + messageCode + " for locale " + locale);

        if (ignoreMissingTranslations) {
            return DEFAULT_MISSING_MESSAGE;
        } else {
            return messageCode;
        }
    }

    @Override
    public String translate(final List<String> messageCodes, final Locale locale, final Object... args) {
        for (String messageCode : messageCodes) {
            String message = translateWithError(messageCode, locale, args);
            if (message != null) {
                return message.trim();
            }
        }

        TRANSLATION_LOG.warn("Missing translation " + messageCodes + " for locale " + locale);

        if (ignoreMissingTranslations) {
            return DEFAULT_MISSING_MESSAGE;
        } else {
            return messageCodes.toString();
        }
    }

    private String translateWithError(final String messageCode, final Locale locale, final Object[] args) {
        return messageSource.getMessage(messageCode, args, null, locale);
    }

    @Override
    public Map<String, String> getCommonsMessages(final Locale locale) {
        Map<String, String> commonsTranslations = new HashMap<String, String>();
        for (String commonMessage : COMMONS_MESSAGES) {
            commonsTranslations.put(commonMessage, translate(commonMessage, locale));
        }
        return commonsTranslations;
    }

    @Override
    public Map<String, String> getSecurityMessages(final Locale locale) {
        Map<String, String> loginTranslations = new HashMap<String, String>();
        for (String loginMessage : SECURITY_MESSAGES) {
            loginTranslations.put(loginMessage, translate(loginMessage, locale));
        }
        return loginTranslations;
    }

    @Override
    public Map<String, String> getDashboardMessages(final Locale locale) {
        Map<String, String> dashboardTranslations = new HashMap<String, String>();
        for (String loginMessage : DASHBOARD_MESSAGES) {
            dashboardTranslations.put(loginMessage, translate(loginMessage, locale));
        }
        return dashboardTranslations;
    }

    @Override
    public String getEntityFieldBaseMessageCode(final DataDefinition dataDefinition, final String fieldName) {
        return dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "." + fieldName;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        getMessagesByPrefix("commons", COMMONS_MESSAGES);
        getMessagesByPrefix("security", SECURITY_MESSAGES);
        getMessagesByPrefix("core.dashboard", DASHBOARD_MESSAGES);
    }

    private void getMessagesByPrefix(final String prefix, final Set<String> messages) {
        try {
            Resource[] resources = applicationContext.getResources("classpath*:locales/*.properties");
            for (Resource resource : resources) {
                getMessagesByPrefix(prefix, messages, resource.getInputStream());
            }
            resources = applicationContext.getResources("WEB-INF/locales/*.properties");
            for (Resource resource : resources) {
                getMessagesByPrefix(prefix, messages, resource.getInputStream());
            }
        } catch (IOException e) {
            LOG.error("Cannot read messages file", e);
        }

        LOG.info("Messages for " + prefix + ": " + messages);
    }

    private void getMessagesByPrefix(final String prefix, final Set<String> messages, final InputStream inputStream)
            throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        for (Object property : properties.keySet()) {
            if (String.valueOf(property).startsWith(prefix)) {
                messages.add((String) property);
            }
        }
    }

    @Override
    public String translateErrorMessage(final ErrorMessage errorMessage, final Locale locale) {
        return translate(errorMessage.getMessage(), locale, (Object[]) errorMessage.getVars());
    }

}
