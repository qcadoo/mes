/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basic.hooks;

import java.util.Currency;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.types.BelongsToType;

@Service
public class ParameterModelHooks {

    private static final String FIELD_CURRENCY = "currency";

    public void setDefaultCurrency(final DataDefinition dataDefinition, final Entity parameter) {
        String defaultCurrencyAlphabeticCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        DataDefinition currencyDataDef = ((BelongsToType) dataDefinition.getField(FIELD_CURRENCY).getType()).getDataDefinition();
        Entity defaultCurrency = currencyDataDef.find()
                .add(SearchRestrictions.eq("alphabeticCode", defaultCurrencyAlphabeticCode)).setMaxResults(1).uniqueResult();
        parameter.setField(FIELD_CURRENCY, defaultCurrency);
    }

}
