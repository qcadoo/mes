/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.qualityControls.constants;

public interface QualityControlsConstants {

    String PLUGIN_IDENTIFIER = "qualityControls";

    // MODEL
    String MODEL_QUALITY_CONTROL = "qualityControl";

    // VIEW
    String VIEW_QUALITY_CONTROL_FOR_ORDER_DETAILS = "qualityControlForOrderDetails";

    String VIEW_QUALITY_CONTROL_FOR_UNIT_DETAILS = "qualityControlForUnitDetails";

    String VIEW_QUALITY_CONTROL_REPORT = "qualityControlReport";

    String VIEW_QUALITY_CONTROLS_FOR_ORDER_LIST = "qualityControlsForOrderList";

    String VIEW_QUALITY_CONTROLS_FOR_UNIT_LIST = "qualityControlsForUnitList";

    String CONTROL_RESULT_TYPE_OBJECTION = "03objection";

    String CONTROL_RESULT_TYPE_INCORRECT = "02incorrect";

    String CONTROL_RESULT_TYPE_CORRECT = "01correct";

    String FIELD_CONTROL_RESULT = "controlResult";
}
