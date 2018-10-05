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
package com.qcadoo.mes.materialRequirementCoverageForOrder;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.OperationType;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.MaterialRequirementCoverageForOrderConstans;

@Service
public class MaterialRequirementCoverageForOrderColumnLoaderServiceImpl implements
        MaterialRequirementCoverageForOrderColumnLoaderService {

    private static final String L_COLUMN_FOR_COVERAGES = "columnForCoveragesForOrder";

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Override
    public void fillColumnsForCoverages(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_COVERAGES);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_COVERAGES, OperationType.ADD, columnAttributes);
        }
    }

    @Override
    public void clearColumnsForCoverages(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_COVERAGES);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_COVERAGES, OperationType.DELETE, columnAttributes);
        }
    }

    private void readData(final String type, final OperationType operation, final Map<String, String> values) {
        if (L_COLUMN_FOR_COVERAGES.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForCoverages(values);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForCoverages(values);
            }
        }
    }

    private void addColumnForCoverages(final Map<String, String> columnAttributes) {
        columnExtensionService.addColumn(MaterialRequirementCoverageForOrderConstans.PLUGIN_IDENTIFIER,
                MaterialRequirementCoverageForOrderConstans.MODEL_COLUMN_FOR_COVERAGES_FOR_ORDER, columnAttributes);
    }

    private void deleteColumnForCoverages(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(MaterialRequirementCoverageForOrderConstans.PLUGIN_IDENTIFIER,
                MaterialRequirementCoverageForOrderConstans.MODEL_COLUMN_FOR_COVERAGES_FOR_ORDER, columnAttributes);
    }

    public boolean isColumnsForCoveragesEmpty() {
        return columnExtensionService.isColumnsEmpty(MaterialRequirementCoverageForOrderConstans.PLUGIN_IDENTIFIER,
                MaterialRequirementCoverageForOrderConstans.MODEL_COLUMN_FOR_COVERAGES_FOR_ORDER);
    }

}
