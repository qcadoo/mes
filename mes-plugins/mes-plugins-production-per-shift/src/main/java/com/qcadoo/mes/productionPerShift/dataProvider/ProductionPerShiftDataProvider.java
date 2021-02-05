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
package com.qcadoo.mes.productionPerShift.dataProvider;

import static com.qcadoo.model.api.search.SearchOrders.desc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchProjections.sum;
import static com.qcadoo.model.api.search.SearchRestrictions.in;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchProjectionList;

/**
 * This service aims to be some common PPS data-access facade which provides common building blocks for retrieving data.
 *
 * @author Marcin Kubala
 * @since 1.3.0
 */
@Service public class ProductionPerShiftDataProvider {

    private static final String ID = "id";

    private static final String DOT = ".";

    /**
     * Alias for technologyOperationComponent. Use this constant when building additional search criterion for
     * #countSumOfQuantities
     */
    public static final String TECHNOLOGY_OPERATION_COMPONENT_ALIAS = "toc_alias";

    /**
     * Alias for ProgressForDay. Use this constant when building additional search criterion for #countSumOfQuantities
     */
    public static final String PROGRESS_FOR_DAY_ALIAS = "pfd_alias";

    /**
     * Alias for dailyProgress. Use this constant when building additional search criterion for #countSumOfQuantities
     */
    public static final String DAILY_PROGRESS_ALIAS = "dp_alias";

    private static final String QUANTITY_SUM_PROJECTION = "quantitySumProjection";

    /**
     * Restrict
     */
    public static final SearchCriterion ONLY_ROOT_OPERATIONS_CRITERIA = isNull(
            TECHNOLOGY_OPERATION_COMPONENT_ALIAS + DOT + TechnologyOperationComponentFields.PARENT);

    @Autowired private DataDefinitionService dataDefinitionService;

    public Set<Long> findIdsOfEffectiveProgressForDay(Entity pps, boolean corrected) {
        Set<Long> pfdIds = Sets.newHashSet();
        if (corrected) {
            pfdIds = pps.getHasManyField(ProductionPerShiftFields.PROGRES_FOR_DAYS).stream()
                    .filter(pfd -> pfd.getBooleanField(ProgressForDayFields.CORRECTED)).map(pfdf -> pfdf.getId())
                    .collect(Collectors.toSet());
        } else {
            pfdIds = pps.getHasManyField(ProductionPerShiftFields.PROGRES_FOR_DAYS).stream()
                    .filter(pfd -> !pfd.getBooleanField(ProgressForDayFields.CORRECTED) == corrected).map(pfdf -> pfdf.getId())
                    .collect(Collectors.toSet());
        }
        return pfdIds;
    }

    public BigDecimal countSumOfQuantities(final Entity pps, boolean corrected) {
        Set<Long> pfdIds = Sets.newHashSet();
        Entity ppsDB = pps.getDataDefinition().get(pps.getId());
        if (corrected) {
            pfdIds = ppsDB.getHasManyField(ProductionPerShiftFields.PROGRES_FOR_DAYS).stream()
                    .filter(pfd -> pfd.getBooleanField(ProgressForDayFields.CORRECTED)).map(pfdf -> pfdf.getId())
                    .collect(Collectors.toSet());
        } else {
            pfdIds = ppsDB.getHasManyField(ProductionPerShiftFields.PROGRES_FOR_DAYS).stream()
                    .filter(pfd -> !pfd.getBooleanField(ProgressForDayFields.CORRECTED)).map(pfdf -> pfdf.getId())
                    .collect(Collectors.toSet());
        }
        if (pfdIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        SearchProjectionList projectionList = list();
        projectionList.add(alias(sum(DAILY_PROGRESS_ALIAS + DOT + DailyProgressFields.QUANTITY), QUANTITY_SUM_PROJECTION));
        // To prevent NPE during conversion to generic entity
        projectionList.add(rowCount());

        SearchCriteriaBuilder scb = getDailyProgressDD().findWithAlias(DAILY_PROGRESS_ALIAS);
        scb.setProjection(projectionList);
        scb.createAlias(DAILY_PROGRESS_ALIAS + DOT + DailyProgressFields.PROGRESS_FOR_DAY, PROGRESS_FOR_DAY_ALIAS,
                JoinType.INNER);
        scb.add(in(PROGRESS_FOR_DAY_ALIAS + DOT + ID, pfdIds));

        scb.addOrder(desc(QUANTITY_SUM_PROJECTION));

        Entity projection = scb.setMaxResults(1).uniqueResult();
        BigDecimal quantitiesSum = projection.getDecimalField(QUANTITY_SUM_PROJECTION);
        return ObjectUtils.defaultIfNull(quantitiesSum, BigDecimal.ZERO);
    }

    public DataDefinition getProductionPerShiftDD() {
        return dataDefinitionService
                .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT);
    }

    private DataDefinition getDailyProgressDD() {
        return dataDefinitionService
                .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_DAILY_PROGRESS);
    }

}
