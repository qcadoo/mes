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
package com.qcadoo.mes.materialRequirementCoverageForOrder.constans;

import java.util.Date;

public class CoverageProduct {

    private Date coverageDate;

    public CoverageProduct(final Date coverageDate) {
        this.coverageDate = dateCopy(coverageDate);
    }

    public Date getCoverageDate() {
        return dateCopy(coverageDate);
    }

    public void setCoverageDate(final Date coverageDate) {
        this.coverageDate = dateCopy(coverageDate);
    }

    private Date dateCopy(final Date date) {
        if (date == null) {
            return null;
        } else {
            return new Date(date.getTime());
        }
    }

}