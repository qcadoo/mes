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
package com.qcadoo.mes.productionPerShift.dataProvider;

import static com.qcadoo.model.api.search.SearchOrders.desc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.groupField;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.max;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchProjections.sum;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.in;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.domain.ProductionPerShiftId;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.tree.domain.TechnologyOperationId;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchProjectionList;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.utils.EntityUtils;

/**
 * This service aims to be some common PPS data-access facade which provides common building blocks for retrieving data.
 * 
 * @author Marcin Kubala
 * @since 1.3.0
 */
@Service
public class ProductionPerShiftDataProvider {

    private static final String PPS_FOR_TOC_ID_QUERY = "select                      \n"
            + "  pps.id as ppsId                                                    \n"
            + "from #productionPerShift_productionPerShift pps                      \n"
            + "  inner join pps.order o                                             \n"
            + "where                                                                \n"
            + "  o.id in (select                                                    \n"
            + "      o.id                                                           \n"
            + "    from #technologies_technologyOperationComponent toc              \n"
            + "      inner join toc.technology tech                                 \n"
            + "      inner join tech.orders o                                       \n"
            + "    where                                                            \n"
            + "      toc.id = :tocId                                                \n"
            + "    order by o.id desc)                                              \n"
            + "order by pps.id desc                                                 \n";

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

    private static final String TECH_ALIAS = "tech";

    private static final String QUANTITY_SUM_PROJECTION = "quantitySumProjection";

    private static final String ID_PROJECTION = "idProjection";

    /**
     * Restrict
     */
    public static final SearchCriterion ONLY_ROOT_OPERATIONS_CRITERIA = isNull(TECHNOLOGY_OPERATION_COMPONENT_ALIAS + DOT
            + TechnologyOperationComponentFields.PARENT);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Returns id of production per shift record, that belongs to the same production order as given technology operation.
     * 
     * @param tocId
     *            identifier of the technology operation component
     * @return production per shift record id or none, if such pps cannot be found
     * @since 1.4
     */
    public Optional<ProductionPerShiftId> find(final TechnologyOperationId tocId) {
        SearchQueryBuilder sqb = getProductionPerShiftDD().find(PPS_FOR_TOC_ID_QUERY).setLong("tocId", tocId.get());
        sqb.setMaxResults(1);
        return Optional.ofNullable(sqb.uniqueResult()).flatMap(e -> Optional.ofNullable((Long) e.getField("ppsId")))
                .map(ProductionPerShiftId::new);
    }

    /**
     * Returns sum of quantities from matching daily shift progress entries. Notice that corrected quantities take precedence over
     * planned ones (see documentation for #findIdsOfEffectiveProgressForDay).<br/>
     * <br/>
     * 
     * By default this method counts quantities for each operations in technology tree. If you want to count only quantities for
     * root operations pass ProductionPerShiftDataProvider#ONLY_ROOT_OPERATIONS_CRITERIA search criterion as a second argument.
     * 
     * @param technologyId
     *            id of technology
     * @param additionalCriteria
     *            optional additional criteria.
     * @return sum of quantities from matching daily shift progresses or BigDecimal#ZERO if there is no matching progress entries.
     * @since 1.3.0
     */
    public BigDecimal countSumOfQuantities(final Long technologyId, final SearchCriterion additionalCriteria) {
        Set<Long> pfdIds = findIdsOfEffectiveProgressForDay(technologyId);

        if (pfdIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        SearchProjectionList projectionList = list();
        projectionList.add(alias(sum(DAILY_PROGRESS_ALIAS + DOT + DailyProgressFields.QUANTITY), QUANTITY_SUM_PROJECTION));
        // To prevent NPE during conversion to generic entity
        projectionList.add(rowCount());

        SearchCriteriaBuilder scb = getDailyProgressDD().findWithAlias(DAILY_PROGRESS_ALIAS);
        scb.setProjection(projectionList);
        scb.createAlias(DAILY_PROGRESS_ALIAS + DOT + DailyProgressFields.PROGRESS_FOR_DAY, PROGRESS_FOR_DAY_ALIAS, JoinType.INNER);
        scb.createAlias(PROGRESS_FOR_DAY_ALIAS + DOT + ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT,
                TECHNOLOGY_OPERATION_COMPONENT_ALIAS, JoinType.INNER);
        scb.add(in(PROGRESS_FOR_DAY_ALIAS + DOT + ID, pfdIds));
        if (additionalCriteria != null) {
            scb.add(additionalCriteria);
        }
        scb.addOrder(desc(QUANTITY_SUM_PROJECTION));

        Entity projection = scb.setMaxResults(1).uniqueResult();
        BigDecimal quantitiesSum = projection.getDecimalField(QUANTITY_SUM_PROJECTION);
        return ObjectUtils.defaultIfNull(quantitiesSum, BigDecimal.ZERO);
    }

    /**
     * This method returns a set of corrected or planned (if corrected progress doesn't exist for given day & operation
     * combination) progress ids for given technology.<br/>
     * <br/>
     * 
     * I assume that each order has its own copy of technology tree.<br/>
     * <br/>
     * 
     * Corrected progresses take precedence over planned ones. For example:<br/>
     * <br/>
     * 
     * If you have defined following planned progresses for some operation OP-1 at 1st, 2nd and 3rd day and you have also progress
     * correction for the same operation at 2nd day, you'll get ids of the following records:
     * <ul>
     * <li>planned progress for 1st day</li>
     * <li>corrected progress for 2nd day</li>
     * <li>planned progress for 3rd day</li>
     * </ul>
     * 
     * @return set of ids for progressForDay entities, related to given technology.
     * @since 1.3.0
     */
    public Set<Long> findIdsOfEffectiveProgressForDay(final Long technologyId) {
        DataDefinition progressForDayDD = getProgressForDayDD();

        SearchProjectionList projectionsList = list();
        projectionsList.add(alias(max(PROGRESS_FOR_DAY_ALIAS + DOT + ID), ID_PROJECTION));
        projectionsList.add(groupField(TECHNOLOGY_OPERATION_COMPONENT_ALIAS + DOT + ID));
        projectionsList.add(groupField(PROGRESS_FOR_DAY_ALIAS + DOT + ProgressForDayFields.ACTUAL_DATE_OF_DAY));

        SearchCriteriaBuilder scb = progressForDayDD.findWithAlias(PROGRESS_FOR_DAY_ALIAS);
        scb.setProjection(projectionsList);
        scb.createAlias(PROGRESS_FOR_DAY_ALIAS + DOT + ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT,
                TECHNOLOGY_OPERATION_COMPONENT_ALIAS, JoinType.INNER);
        scb.createAlias(TECHNOLOGY_OPERATION_COMPONENT_ALIAS + DOT + TechnologyOperationComponentFields.TECHNOLOGY, TECH_ALIAS,
                JoinType.INNER);
        scb.add(eq(TECH_ALIAS + DOT + ID, technologyId));
        scb.addOrder(desc(ID_PROJECTION));

        List<Entity> idsProjection = scb.list().getEntities();

        return Sets.newHashSet(EntityUtils.<Long> getFieldsView(idsProjection, ID_PROJECTION));
    }

    private DataDefinition getProductionPerShiftDD() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT);
    }

    private DataDefinition getProgressForDayDD() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
    }

    private DataDefinition getDailyProgressDD() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_DAILY_PROGRESS);
    }
}
