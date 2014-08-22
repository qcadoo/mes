/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.basic;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRatesUpdateServiceImpl implements ExchangeRatesUpdateService {

    private ExchangeRatesNbpService nbpService;
    private DataDefinitionService dataDefinitionService;

    @Autowired
    public ExchangeRatesUpdateServiceImpl(ExchangeRatesNbpService nbpService,
                                          DataDefinitionService dataDefinitionService) {
        this.nbpService = nbpService;
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override @Async @Scheduled(cron = ExchangeRatesNbpService.CRON_LAST_ALL)
    public void update() {
        updateEntitiesExchangeRates(nbpService.get(ExchangeRatesNbpService.NbpProperties.LAST_A));
        updateEntitiesExchangeRates(nbpService.get(ExchangeRatesNbpService.NbpProperties.LAST_B));
        updateEntitiesExchangeRates(nbpService.get(ExchangeRatesNbpService.NbpProperties.LAST_C));
    }

    private void updateEntitiesExchangeRates(Map<String, BigDecimal> exRates) {
        List<Entity> entities = getCurrencyDD().find().list().getEntities();
        for (Entity entity : entities) {
            String isoCode = (String) entity.getField(CurrencyFields.ALPHABETIC_CODE);
            if(exRates.containsKey(isoCode))
                updateExchangeRate(entity, exRates.get(isoCode));
        }
    }

    private void updateExchangeRate(Entity entity, BigDecimal exRate) {
        entity.setField(CurrencyFields.EXCHANGE_RATE, exRate);
        getCurrencyDD().save(entity);
    }

    private DataDefinition getCurrencyDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
    }
}
