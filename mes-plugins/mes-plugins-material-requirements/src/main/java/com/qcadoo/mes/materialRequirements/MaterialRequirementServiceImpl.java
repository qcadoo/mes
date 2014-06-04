/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.materialRequirements;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.materialRequirements.print.pdf.MaterialRequirementPdfService;
import com.qcadoo.mes.materialRequirements.print.xls.MaterialRequirementXlsService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;

@Service
public class MaterialRequirementServiceImpl implements MaterialRequirementService {

    @Autowired
    private FileService fileService;

    @Autowired
    private MaterialRequirementPdfService materialRequirementPdfService;

    @Autowired
    private MaterialRequirementXlsService materialRequirementXlsService;

    @Override
    public boolean checkIfInputProductsRequiredForTypeIsSelected(final DataDefinition entityDD, final Entity entity,
            final String fieldName, final String errorMessage) {
        String inputProductsRequiredForType = entity.getStringField(fieldName);

        if (inputProductsRequiredForType == null) {
            entity.addError(entityDD.getField(fieldName), errorMessage);

            return false;
        }

        return true;
    }

    @Override
    public void setInputProductsRequiredForTypeDefaultValue(final Entity entity, final String fieldName, final String fieldValue) {
        String inputProductsRequiredForType = entity.getStringField(fieldName);

        if (inputProductsRequiredForType == null) {
            entity.setField(fieldName, fieldValue);
        }
    }

    @Override
    public MrpAlgorithm getDefaultMrpAlgorithm() {
        return MrpAlgorithm.ONLY_COMPONENTS;
    }

    @Override
    public void generateMaterialRequirementDocuments(final ComponentState state, final Entity materialRequirement)
            throws IOException, DocumentException {
        Entity materialRequirementWithFileName = fileService.updateReportFileName(materialRequirement,
                MaterialRequirementFields.DATE, "materialRequirements.materialRequirement.report.fileName");

        materialRequirementPdfService.generateDocument(materialRequirementWithFileName, state.getLocale());
        materialRequirementXlsService.generateDocument(materialRequirementWithFileName, state.getLocale());
    }

}
