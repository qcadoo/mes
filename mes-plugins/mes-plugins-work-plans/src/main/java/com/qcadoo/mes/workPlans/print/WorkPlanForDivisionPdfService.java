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
package com.qcadoo.mes.workPlans.print;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.pdf.document.WorkPlanPdfForDivision;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.factory.GroupingContainerFactory;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.report.api.pdf.PdfDocumentWithWriterService;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkPlanForDivisionPdfService extends PdfDocumentWithWriterService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private GroupingContainerFactory groupingContainerFactory;

    @Autowired
    private WorkPlanPdfForDivision workPlanPdfForDivision;

    @Autowired
    private ProductQuantitiesServiceImpl productQuantitiesServiceImpl;

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

    @Autowired
    private WorkPlanPdfService workPlanPdfService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("workPlans.workPlan.report.title", locale);
    }

    @Override
    public void buildPdfContent(final PdfWriter writer, final Document document, final Entity workPlan, final Locale locale)
            throws DocumentException {

        GroupingContainer groupingContainer = groupingContainerFactory.create(workPlan, locale);

        for (Entity order : orders(workPlan)) {
            for (Entity operationComponent : operationComponents(technology(order))) {
                List<Entity> productionCountingQuantitiesIn = workPlanPdfService.getProductionCountingQuantitiesIn(order, operationComponent);
                List<Entity> productionCountingQuantitiesOut = workPlanPdfService.getProductionCountingQuantitiesOut(order, operationComponent);
                groupingContainer.add(order, operationComponent, productionCountingQuantitiesIn, productionCountingQuantitiesOut);
            }
        }

        workPlanPdfForDivision.print(writer, groupingContainer, workPlan, document, locale);

    }

    private List<Entity> operationComponents(Entity technology) {
        return entityTreeUtilsService.getSortedEntities(technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS));
    }

    private Entity technology(Entity order) {
        return order.getBelongsToField(OrderFields.TECHNOLOGY);
    }

    private EntityList orders(Entity workPlan) {
        return workPlan.getHasManyField(WorkPlanFields.ORDERS);
    }


}
