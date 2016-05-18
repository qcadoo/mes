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
package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.workPlans.constants.OperationFieldsWP;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfHelper;

@Component
public class OperationAdditionalFields {

    private final PdfHelper pdfHelper;

    private final TranslationService translationService;

    @Autowired
    public OperationAdditionalFields(PdfHelper pdfHelper, TranslationService translationService) {
        this.pdfHelper = pdfHelper;
        this.translationService = translationService;
    }

    public void print(Entity operationComponent, Document document, Locale locale) throws DocumentException {
        Optional<String> imageUrlInWorkPlan = getImageUrlInWorkPlan(operationComponent);

        if (!imageUrlInWorkPlan.isPresent()) {
            return;
        }

        document.add(new Paragraph(title(locale), FontUtils.getDejavuBold10Dark()));
        pdfHelper.addImage(document, imageUrlInWorkPlan.get());
        document.add(Chunk.NEXTPAGE);

    }

    private String title(Locale locale) {
        return translationService.translate("workPlans.workPlan.report.additionalFields", locale);
    }

    private Optional<String> getImageUrlInWorkPlan(final Entity technologyOperationComponent) {
        return Optional.fromNullable(imagePath(technologyOperationComponent));
    }

    private String imagePath(Entity technologyOperationComponent) {
        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        return operation.getStringField(OperationFieldsWP.IMAGE_URL_IN_WORK_PLAN);
    }

}
