/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.9
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);

    public Entity getCurrentCurrency() {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
        Entity currency = dd.find().add(SearchRestrictions.eq("isActive", true)).uniqueResult();

        if (LOG.isDebugEnabled()) {
            LOG.debug("actual currency = " + currency);
        }

        if (currency != null) {
            return currency;
        }

        String alphabeticCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        currency = dd.find().add(SearchRestrictions.eq("alphabeticCode", alphabeticCode)).uniqueResult();
        currency.setField("isActive", true);
        return dd.save(currency);
    }

    public String getCurrencyAlphabeticCode() {
        return getCurrentCurrency().getField("alphabeticCode").toString();
    }

}
