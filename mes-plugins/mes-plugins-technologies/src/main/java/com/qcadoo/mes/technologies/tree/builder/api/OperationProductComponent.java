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
