/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.workPlans;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.mes.workPlans.print.WorkPlanPdfService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;

@Service
public class WorkPlanServiceImpl implements WorkPlanService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private WorkPlanPdfService workPlanPdfService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private FileService fileService;

    @Override
    @Transactional
    public final void generateWorkPlanDocuments(final ComponentState state, final Entity workPlan) throws IOException,
            DocumentException {
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        Entity workPlanWithFilename = fileService.updateReportFileName(workPlan, "date", "workPlans.workPlan.report.fileName");
        workPlanPdfService.generateDocument(workPlanWithFilename, company, state.getLocale());
    }

    @Override
    public final Entity generateWorkPlanEntity(final List<Entity> orders) {
        Entity workPlan = getWorkPlanDataDefinition().create();
        workPlan.setField("orders", orders);
        workPlan.setField("name", generateNameForWorkPlan());
        workPlan.setField("type", WorkPlanType.NO_DISTINCTION.getStringValue());

        return workPlan.getDataDefinition().save(workPlan);
    }

    @Override
    public final List<Entity> getSelectedOrders(final Set<Long> selectedOrderIds) {
        List<Entity> orders = Lists.newArrayList();
        if (selectedOrderIds.isEmpty()) {
            return orders;
        }
        SearchCriteriaBuilder criteria = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find();
        criteria.add(SearchRestrictions.in("id", selectedOrderIds));
        SearchResult result = criteria.list();
        if (result.getTotalNumberOfEntities() == 0) {
            return orders;
        }
        orders.addAll(result.getEntities());
        return orders;
    }

    @Override
    public final Entity getWorkPlan(final Long workPlanId) {
        return getWorkPlanDataDefinition().get(workPlanId);
    }

    private String generateNameForWorkPlan() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, LocaleContextHolder.getLocale());

        return translationService.translate("workPlans.workPlan.defaults.name", LocaleContextHolder.getLocale(),
                dateFormat.format(currentDate));
    }

    private DataDefinition getWorkPlanDataDefinition() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN);
    }
}
