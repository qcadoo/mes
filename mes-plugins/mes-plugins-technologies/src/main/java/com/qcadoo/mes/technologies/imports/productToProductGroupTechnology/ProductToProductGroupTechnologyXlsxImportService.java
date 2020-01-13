package com.qcadoo.mes.technologies.imports.productToProductGroupTechnology;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.technologies.constants.ProductToProductGroupFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductToProductGroupTechnologyXlsxImportService extends XlsxImportService {

    @Override
    public void validateEntity(final Entity entity, final DataDefinition dataDefinition) {
        if (entity.getBelongsToField(ProductToProductGroupFields.PRODUCT_FAMILY) != null
                && !ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()
                        .equals(entity.getBelongsToField(ProductToProductGroupFields.PRODUCT_FAMILY)
                                .getStringField(ProductFields.ENTITY_TYPE))) {
            entity.addError(dataDefinition.getField(ProductToProductGroupFields.PRODUCT_FAMILY),
                    "technologies.productToProductGroupTechnology.validate.error.productIsntProductFamily");
        }
        if (entity.getBelongsToField(ProductToProductGroupFields.PRODUCT_FAMILY) != null
                && entity.getBelongsToField(ProductToProductGroupFields.ORDER_PRODUCT) != null
                && !entity.getBelongsToField(ProductToProductGroupFields.PRODUCT_FAMILY).equals(entity
                        .getBelongsToField(ProductToProductGroupFields.ORDER_PRODUCT).getBelongsToField(ProductFields.PARENT))) {
            entity.addError(dataDefinition.getField(ProductToProductGroupFields.ORDER_PRODUCT),
                    "technologies.productToProductGroupTechnology.validate.error.productBelongsToAnotherFamily");
        }
    }
}
