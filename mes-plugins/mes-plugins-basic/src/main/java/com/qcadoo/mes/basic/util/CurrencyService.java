/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.basic.util;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.qcadoo.mes.basic.constants.BasicConstants.PLUGIN_IDENTIFIER;

/**
 * Service for accessing currently used currency
 * 
 */
@Service
public class CurrencyService {

    public static final String PLN = "PLN";

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Returns currently used currency {@link Entity}.
     * 
     * @return currently used currency {@link Entity}.
     */
    public Entity getCurrentCurrency() {
        return parameterService.getParameter().getBelongsToField(CurrencyFields.CURRENCY);
    }

    public Entity getCurrencyById(Long id) {
        DataDefinition currencyDataDef = dataDefinitionService.get(PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
        return currencyDataDef.get(id);
    }

    public Entity getCurrencyByAlphabeticCode(String alphabeticCode) {
        DataDefinition currencyDataDef = dataDefinitionService.get(PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
        return currencyDataDef.find().add(SearchRestrictions.eq(CurrencyFields.ALPHABETIC_CODE, alphabeticCode)).setMaxResults(1)
                .uniqueResult();
    }

    /**
     * Returns alphabetic (ISO-4217) code for currently used currency.
     * 
     * @return alphabetic (ISO-4217) code for currently used currency.
     */
    public String getCurrencyAlphabeticCode() {
        if (getCurrentCurrency() == null) {
            return "";
        }
        return getCurrentCurrency().getStringField(CurrencyFields.ALPHABETIC_CODE);
    }

    public BigDecimal getConvertedValue(final BigDecimal value, final Entity currency) {
        BigDecimal exRate = currency.getDecimalField(CurrencyFields.EXCHANGE_RATE);

        return Optional.ofNullable(value)
                .map(v -> exRateExists(exRate)
                        ? numberService.setScaleWithDefaultMathContext(v.multiply(exRate, numberService.getMathContext()))
                        : v)
                .orElse(null);

    }

    private boolean exRateExists(final BigDecimal exRate) {
        return exRate.compareTo(BigDecimal.ZERO) > 0;
    }

    public void clearExchangeRate() {
        DataDefinition currencyDataDef = dataDefinitionService.get(PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
        List<Entity> entities = currencyDataDef.find().list().getEntities();
        entities.forEach(c -> {
            c.setField(CurrencyFields.EXCHANGE_RATE, BigDecimal.ONE);
            currencyDataDef.save(c);
        });
    }
}
