/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.ParameterFieldsAGFO;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordForOrderTreatment;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;

@Service
public class ParameterHooksAGFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void addFieldsForParameter(final DataDefinition parameterDD, final Entity parameter) {
        parameter.setField(ParameterFieldsAGFO.TRACKING_RECORD_FOR_ORDER_TREATMENT,
                TrackingRecordForOrderTreatment.DURING_PRODUCTION.getStringValue());
        parameter.setField(ParameterFieldsAGFO.BATCH_NUMBER_REQUIRED_PRODUCTS, false);
    }

    public void setUsedInForNumberPattern(final DataDefinition parameterDD, final Entity parameter) {
        Entity numberPattern = parameter.getBelongsToField(ParameterFieldsAGFO.NUMBER_PATTERN);
        DataDefinition numberPatternDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_NUMBER_PATTERN);
        String usedInValue = translationService.translate("basic.parameter.numberPattern.usedIn.value",
                LocaleContextHolder.getLocale());
        Entity numberPatternWithUsedIn = numberPatternDD.find()
                .add(SearchRestrictions.eq(NumberPatternFields.USED_IN, usedInValue)).uniqueResult();
        if (numberPattern != null) {
            if (!numberPattern.getBooleanField(NumberPatternFields.USED)) {
                createSequence(numberPattern.getStringField(NumberPatternFields.NUMBER));
            }
            numberPattern.setField(NumberPatternFields.USED, true);
            if (numberPatternWithUsedIn != null && !numberPatternWithUsedIn.getId().equals(numberPattern.getId())) {
                numberPatternWithUsedIn.setField(NumberPatternFields.USED_IN, null);
                numberPatternDD.save(numberPatternWithUsedIn);
                numberPattern.setField(NumberPatternFields.USED_IN, usedInValue);
                numberPatternDD.save(numberPattern);
            } else if (numberPatternWithUsedIn == null) {
                numberPattern.setField(NumberPatternFields.USED_IN, usedInValue);
                numberPatternDD.save(numberPattern);
            }
        } else if (numberPatternWithUsedIn != null) {
            numberPatternWithUsedIn.setField(NumberPatternFields.USED_IN, null);
            numberPatternDD.save(numberPatternWithUsedIn);
        }
    }

    private void createSequence(String number) {
        jdbcTemplate.execute("CREATE SEQUENCE number_pattern_" + number + "_seq", (PreparedStatementCallback) PreparedStatement::executeUpdate);
    }
}
