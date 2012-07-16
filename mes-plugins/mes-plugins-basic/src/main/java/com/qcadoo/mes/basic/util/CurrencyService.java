/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.Entity;

/**
 * Service for accessing currently used currency
 * 
 */
@Service
public class CurrencyService {

    @Autowired
    private ParameterService parameterService;

    /**
     * Returns currently used currency {@link Entity}.
     * 
     * @return currently used currency {@link Entity}.
     */
    public Entity getCurrentCurrency() {
        return parameterService.getParameter().getBelongsToField("currency");
    }

    /**
     * Returns alphabetic (ISO-4217) code for currently used currency.
     * 
     * @return alphabetic (ISO-4217) code for currently used currency.
     */
    public String getCurrencyAlphabeticCode() {
        return getCurrentCurrency().getStringField("alphabeticCode");
    }
}
