/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.samples;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.samples.constants.SamplesConstants;

@Component
public class MinimalSamplesLoader extends SamplesLoader {

    @Override
    void loadData(final String dataset, final String locale) {

        if (isEnabled(SamplesConstants.BASIC_PLUGIN_IDENTIFIER)) {
            readDataFromXML(dataset, "activeCurrency", locale);
            readDataFromXML(dataset, "dictionaries", locale);
            readDataFromXML(dataset, "shifts", locale);
            readDataFromXML(dataset, "company", locale);
        }

    }

}
