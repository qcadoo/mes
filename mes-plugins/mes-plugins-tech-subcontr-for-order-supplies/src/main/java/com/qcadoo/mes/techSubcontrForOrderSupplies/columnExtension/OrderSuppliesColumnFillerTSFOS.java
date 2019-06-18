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
package com.qcadoo.mes.techSubcontrForOrderSupplies.columnExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orderSupplies.print.MaterialRequirementCoverageColumnFiller;
import com.qcadoo.mes.techSubcontrForOrderSupplies.constants.CoverageProductFieldsTSFOS;
import com.qcadoo.model.api.Entity;

@Component
public class OrderSuppliesColumnFillerTSFOS implements MaterialRequirementCoverageColumnFiller {

    @Autowired
    private TranslationService translationService;

    @Override
    public Map<Entity, Map<String, String>> getCoverageProductsColumnValues(final List<Entity> coverageProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity coverageProduct : coverageProducts) {
            if (!values.containsKey(coverageProduct)) {
                values.put(coverageProduct, new HashMap<String, String>());
            }

            fillIsSubcontracted(values, coverageProduct);
            fillIsPurchased(values, coverageProduct);
        }

        return values;
    }

    private void fillIsSubcontracted(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        Boolean isSubcontracted = null;

        if (coverageProduct == null) {
            isSubcontracted = false;
        } else {
            isSubcontracted = coverageProduct.getBooleanField(CoverageProductFieldsTSFOS.IS_SUBCONTRACTED);
        }

        values.get(coverageProduct).put("isSubcontracted", getStringFromBoolean(isSubcontracted));
    }

    private void fillIsPurchased(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        Boolean isPurchased = null;

        if (coverageProduct == null) {
            isPurchased = false;
        } else {
            isPurchased = coverageProduct.getBooleanField(CoverageProductFieldsTSFOS.IS_PURCHASED);
        }

        values.get(coverageProduct).put("isPurchased", getStringFromBoolean(isPurchased));
    }

    private String getStringFromBoolean(final boolean isSubcontractedOrPurchased) {
        return (isSubcontractedOrPurchased) ? translationService.translate("qcadooView.true", LocaleContextHolder.getLocale())
                : translationService.translate("qcadooView.false", LocaleContextHolder.getLocale());
    }

}
