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
package com.qcadoo.mes.workPlans.print;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.pdf.document.WorkPlanPdf;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.factory.GroupingContainerFactory;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.report.api.pdf.PdfDocumentWithWriterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class WorkPlanPdfService extends PdfDocumentWithWriterService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private GroupingContainerFactory groupingContainerFactory;

    @Autowired
    private WorkPlanPdf workPlanPdf;

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("workPlans.workPlan.report.title", locale);
    }

    @Override
    public void buildPdfContent(final PdfWriter writer, final Document document, final Entity workPlan, final Locale locale)
            throws DocumentException {

        GroupingContainer groupingContainer = groupingContainerFactory.create(workPlan, locale);

        List<Entity> orders = orders(workPlan);
        for (Entity order : orders)
            for (Entity operationComponent : operationComponents(technology(order)))
                groupingContainer.add(order, operationComponent);

        workPlanPdf.print(writer, groupingContainer, workPlan, document, locale);

    }

    private EntityList operationComponents(Entity technology) {
        return technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
    }

    private Entity technology(Entity order) {
        return order.getBelongsToField(OrderFields.TECHNOLOGY);
    }

    private EntityList orders(Entity workPlan) {
        return workPlan.getHasManyField(WorkPlanFields.ORDERS);
    }


}
