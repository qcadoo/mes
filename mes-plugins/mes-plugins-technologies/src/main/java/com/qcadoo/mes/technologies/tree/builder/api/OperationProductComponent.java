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
package com.qcadoo.mes.technologies.tree.builder.api;

import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;

/**
 * Operation product component
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public interface OperationProductComponent extends EntityWrapper {

    enum OperationCompType {
        INPUT(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT, OperationProductInComponentFields.PRODUCT,
                OperationProductInComponentFields.QUANTITY), OUTPUT(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT,
                OperationProductOutComponentFields.PRODUCT, OperationProductOutComponentFields.QUANTITY);

        private final String modelName;

        private final String productFieldName;

        private final String quantityFieldName;

        private OperationCompType(final String modelName, final String productFieldName, final String quantityFieldName) {
            this.modelName = modelName;
            this.productFieldName = productFieldName;
            this.quantityFieldName = quantityFieldName;
        }

        public String getModelName() {
            return this.modelName;
        }

        public String getProductFieldName() {
            return productFieldName;
        }

        public String getQuantityFieldName() {
            return quantityFieldName;
        }
    }

    void setField(final String name, final Object value);

}
