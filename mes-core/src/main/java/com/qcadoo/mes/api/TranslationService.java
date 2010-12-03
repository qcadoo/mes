/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.validators.ErrorMessage;

/**
 * Service for getting translations.
 * 
 * @apiviz.uses com.qcadoo.mes.model.DataDefinition
 * @apiviz.uses com.qcadoo.mes.model.validators.ErrorMessage
 */
public interface TranslationService {

    /**
     * Return all translations which message code starts with "commons.".
     * 
     * @param locale
     *            locale
     * @return the commons translations
     */
    Map<String, String> getCommonsMessages(Locale locale);

    /**
     * Return all translations which message code starts with "security.".
     * 
     * @param locale
     *            locale
     * @return the security translations
     */
    Map<String, String> getSecurityMessages(Locale locale);

    /**
     * Return all translations which message code starts with "core.dashboard.".
     * 
     * @param locale
     *            locale
     * @return the dashboard translations
     */
    Map<String, String> getDashboardMessages(Locale locale);

    /**
     * Translate given code into the locale using the args.
     * 
     * @param messageCode
     *            message's code
     * @param locale
     *            locale
     * @param args
     *            message's args
     * @return the translation
     */
    String translate(String messageCode, Locale locale, Object... args);

    /**
     * Translate given codes into the locale using the args. First translated code will be returned.
     * 
     * @param messageCodes
     *            message's codes
     * @param locale
     *            locale
     * @param args
     *            message's args
     * @return the translation
     */
    String translate(List<String> messageCodes, Locale locale, Object... args);

    /**
     * Construct code for given data definition and field name.
     * 
     * @param dataDefinition
     *            data definition
     * @param fieldName
     *            field name
     * @return the message's code
     */
    String getEntityFieldBaseMessageCode(DataDefinition dataDefinition, String fieldName);

    /**
     * Translate the validation error into the locale.
     * 
     * @param validationError
     *            validation error
     * @param locale
     *            locale
     * @return the translation
     */
    String translateErrorMessage(ErrorMessage validationError, Locale locale);
}
