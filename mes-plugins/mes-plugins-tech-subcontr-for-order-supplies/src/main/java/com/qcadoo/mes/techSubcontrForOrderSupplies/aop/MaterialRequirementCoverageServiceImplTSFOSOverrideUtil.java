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
package com.qcadoo.mes.techSubcontrForOrderSupplies.aop;

import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingEventType;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingFields;
import com.qcadoo.mes.orderSupplies.coverage.CoverageProductForDelivery;
import com.qcadoo.mes.orderSupplies.coverage.CoverageProductForOrder;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.OrderedProductFieldsTSFD;
import com.qcadoo.mes.techSubcontrForOrderSupplies.constants.CoverageProductFieldsTSFOS;
import com.qcadoo.mes.techSubcontrForOrderSupplies.constants.CoverageProductLoggingFieldsTSFOS;
import com.qcadoo.mes.techSubcontrForOrderSupplies.constants.TechSubcontrForOrderSuppliesConstants;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyOperationComponentFieldsTS;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginStateResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialRequirementCoverageServiceImplTSFOSOverrideUtil {

    @Autowired
    private PluginStateResolver pluginStateResolver;

    public boolean shouldOverride() {
        return pluginStateResolver.isEnabled(TechSubcontrForOrderSuppliesConstants.PLUGIN_IDENTIFIER);
    }

    public void createCoverageProductLoggingForDeliveryWithSubcontractedOperation(
            final CoverageProductForDelivery coverageProductForDelivery, final Entity coverageProductLogging) {

        coverageProductLogging.setField(CoverageProductLoggingFieldsTSFOS.SUBCONTRACTED_OPERATION, coverageProductForDelivery
                .getDeliveryProduct().getBelongsToField(OrderedProductFieldsTSFD.OPERATION));
    }

    public void createCoverageProductLoggingForOrderWithSubcontractedOperation(
            final CoverageProductForOrder coverageProductForOrder, final Entity coverageProductLogging) {
        coverageProductLogging.setField(CoverageProductLoggingFieldsTSFOS.SUBCONTRACTED_OPERATION,
                getSubcontractedOperationForOrder(coverageProductForOrder));
    }

    private Entity getSubcontractedOperationForOrder(final CoverageProductForOrder coverageProductForOrder) {
        Entity technologyOperationComponent = coverageProductForOrder.getTechnologyOperationComponent();
        Entity product = coverageProductForOrder.getProduct();

        List<Entity> children = technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.CHILDREN).find()
                .add(SearchRestrictions.eq(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING, true)).list().getEntities();

        for (Entity child : children) {
            Entity operationProductOutComponent = child
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).find()
                    .add(SearchRestrictions.belongsTo(OperationProductOutComponentFields.PRODUCT, product)).setMaxResults(1)
                    .uniqueResult();

            if (operationProductOutComponent != null) {
                return child.getBelongsToField(TechnologyOperationComponentFields.OPERATION);
            }
        }

        return null;
    }

    public void fillIsSubcontractedAndIsPurchased(final List<Entity> coverageProducts) {
        for (Entity coverageProduct : coverageProducts) {
            coverageProduct.setField(CoverageProductFieldsTSFOS.IS_SUBCONTRACTED,
                    checkIfThereAreSubcontractedProducts(coverageProduct));
            coverageProduct.setField(CoverageProductFieldsTSFOS.IS_PURCHASED, checkIfThereArePurchasedProducts(coverageProduct));
        }
    }

    private boolean checkIfThereAreSubcontractedProducts(final Entity coverageProduct) {
        List<Entity> coverageProductLoggings = coverageProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS);

        boolean isSubcontracted = false;

        for (Entity coverageProductLogging : coverageProductLoggings) {
            Entity subcontractedOperation = coverageProductLogging
                    .getBelongsToField(CoverageProductLoggingFieldsTSFOS.SUBCONTRACTED_OPERATION);
            String eventType = coverageProductLogging.getStringField(CoverageProductLoggingFields.EVENT_TYPE);

            if (!CoverageProductLoggingEventType.WAREHOUSE_STATE.getStringValue().equals(eventType)
                    && subcontractedOperation != null) {
                isSubcontracted = true;

                break;
            }
        }

        return isSubcontracted;
    }

    private boolean checkIfThereArePurchasedProducts(final Entity coverageProduct) {
        List<Entity> coverageProductLoggings = coverageProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS);

        boolean isPurchased = false;

        for (Entity coverageProductLogging : coverageProductLoggings) {
            Entity subcontractedOperation = coverageProductLogging
                    .getBelongsToField(CoverageProductLoggingFieldsTSFOS.SUBCONTRACTED_OPERATION);
            String eventType = coverageProductLogging.getStringField(CoverageProductLoggingFields.EVENT_TYPE);

            if (!CoverageProductLoggingEventType.WAREHOUSE_STATE.getStringValue().equals(eventType)
                    && subcontractedOperation == null) {
                isPurchased = true;

                break;
            }
        }

        return isPurchased;
    }

}
