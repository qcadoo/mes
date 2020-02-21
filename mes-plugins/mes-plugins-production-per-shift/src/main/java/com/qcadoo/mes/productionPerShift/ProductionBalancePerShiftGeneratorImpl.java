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
package com.qcadoo.mes.productionPerShift;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.productionPerShift.dataProvider.PlannedProgressDataProvider;
import com.qcadoo.mes.productionPerShift.dataProvider.RegisteredProgressDataProvider;
import com.qcadoo.mes.productionPerShift.domain.ProductionProgress;
import com.qcadoo.mes.productionPerShift.domain.ProductionProgressScope;
import com.qcadoo.mes.productionPerShift.domain.QuantitiesBalance;
import com.qcadoo.mes.productionPerShift.factory.BalanceEntityFactory;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductionBalancePerShiftGeneratorImpl implements ProductionBalancePerShiftGenerator {

    @Autowired
    private RegisteredProgressDataProvider registeredProgressDataProvider;

    @Autowired
    private PlannedProgressDataProvider plannedProgressDataProvider;

    @Autowired
    private BalanceEntityFactory balanceEntityFactory;

    @Override
    @Transactional
    public List<Entity> generate(final BalanceGenerationStrategy generationParameters) {
        Collection<ProductionProgress> registered = registeredProgressDataProvider.find(generationParameters.getSearchInterval());
        Map<ProductionProgressScope, BigDecimal> registeredByScope = sumQuantities(groupByScope(registered));

        Collection<ProductionProgress> planned = plannedProgressDataProvider.find(generationParameters.getSearchInterval());
        Map<ProductionProgressScope, BigDecimal> plannedByScope = sumQuantities(groupByScope(planned));

        Set<ProductionProgressScope> combinedScopes = combineScopes(generationParameters, registeredByScope, plannedByScope);

        List<Entity> balanceEntities = Lists.newLinkedList();

        for (ProductionProgressScope scope : combinedScopes) {
            BigDecimal plannedQuantity = plannedByScope.get(scope);
            BigDecimal registeredQuantity = registeredByScope.get(scope);
            QuantitiesBalance balance = new QuantitiesBalance(plannedQuantity, registeredQuantity);
            if (generationParameters.balanceMatchesRequirements(balance)) {
                Entity balanceEntity = balanceEntityFactory.create(scope, balance);
                balanceEntities.add(balanceEntity);
            }
        }

        return balanceEntities;
    }

    private Set<ProductionProgressScope> combineScopes(BalanceGenerationStrategy generationParameters,
            Map<ProductionProgressScope, BigDecimal> registeredByScope, Map<ProductionProgressScope, BigDecimal> plannedByScope) {
        Set<ProductionProgressScope> registeredScopes = registeredByScope.keySet();
        Set<ProductionProgressScope> plannedScopes = plannedByScope.keySet();
        return generationParameters.combineScopes(plannedScopes, registeredScopes);
    }

    private Multimap<ProductionProgressScope, BigDecimal> groupByScope(final Collection<ProductionProgress> progresses) {
        Multimap<ProductionProgressScope, BigDecimal> quantitiesByScope = HashMultimap.create();
        for (ProductionProgress progress : progresses) {
            quantitiesByScope.put(progress.getScope(), progress.getQuantity());
        }
        return quantitiesByScope;
    }

    private Map<ProductionProgressScope, BigDecimal> sumQuantities(final Multimap<ProductionProgressScope, BigDecimal> multimap) {
        return Maps.transformValues(multimap.asMap(), new Function<Collection<BigDecimal>, BigDecimal>() {

            @Override
            public BigDecimal apply(final Collection<BigDecimal> quantities) {
                BigDecimal sum = BigDecimal.ZERO;
                // for sonar's sake..
                if (quantities != null) {
                    for (BigDecimal quantity : quantities) {
                        if (quantity != null) {
                            sum = sum.add(quantity);
                        }
                    }
                }
                return sum;
            }
        });
    }
}
