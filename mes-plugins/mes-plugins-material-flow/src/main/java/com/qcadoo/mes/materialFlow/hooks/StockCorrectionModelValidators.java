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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.LocationType.CONTROL_POINT;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.LOCATION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StockCorrectionModelValidators {

    public boolean validateStockCorrection(final DataDefinition stockCorrectionDD, final Entity stockCorrection) {
        Entity location = stockCorrection.getBelongsToField(LOCATION);

        if (location != null) {
            String locationType = location.getStringField(TYPE);

            if (!CONTROL_POINT.getStringValue().equals(locationType)) {
                stockCorrection.addError(stockCorrectionDD.getField(LOCATION),
                        "materialFlow.validate.global.error.locationIsNotSimpleControlPoint");

                return false;
            }
        }

        return true;
    }

    public boolean checkIfLocationHasExternalNumber(final DataDefinition stockCorrectionDD, final Entity stockCorrection) {
        if (stockCorrection.getBelongsToField(LOCATION).getStringField("externalNumber") != null) {
            stockCorrection.addError(stockCorrectionDD.getField(LOCATION),
                    "materialFlow.validate.global.error.locationHasExternalNumber");
            return false;
        }
        return true;
    }

}
