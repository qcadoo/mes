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
package com.qcadoo.mes.basic.util;

import java.util.Currency;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class CurrencyService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    public Entity getCurrentCurrency() {
        if (getCurrencyEnabled() == null) {
            setCurrencyEnabled(getCurrencyFromLocale());
        }

        return getCurrencyEnabled();
    }

    public Entity getCurrencyFromLocale() {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);

        String alphabeticCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        Entity currency = dd.find().add(SearchRestrictions.eq("alphabeticCode", alphabeticCode)).uniqueResult();

        return currency;
    }

    public Entity getCurrencyEnabled() {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);

        Entity currency = dd.find().add(SearchRestrictions.eq("isActive", true)).uniqueResult();

        return currency;
    }

    public String getCurrencyAlphabeticCode() {
        return getCurrentCurrency().getField("alphabeticCode").toString();
    }

    public void setCurrencyEnabled(Entity currency) {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);

        currency.setField("isActive", true);

        dd.save(currency);
    }

    public void setCurrencyDisabled(Entity currency) {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);

        currency.setField("isActive", false);

        dd.save(currency);
    }

    public void changeCurrentCurrency(final DataDefinition dd, final Entity entity) {
        Entity oldCurrency = getCurrentCurrency();
        Entity newCurrency = entity.getBelongsToField("currency");

        if (newCurrency == null) {
            newCurrency = oldCurrency;
        }

        setCurrencyDisabled(oldCurrency);
        setCurrencyEnabled(newCurrency);
    }
}
