package com.qcadoo.mes.technologies.imports.productToProductGroupTechnology;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.mes.technologies.constants.ProductToProductGroupFields;

@Component
public class ProductToProductGroupTechnologyCellBinderRegistry {

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser productCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(ProductToProductGroupFields.FINAL_PRODUCT, productCellParser));
        cellBinderRegistry.setCellBinder(required(ProductToProductGroupFields.PRODUCT_FAMILY, productCellParser));
        cellBinderRegistry.setCellBinder(required(ProductToProductGroupFields.ORDER_PRODUCT, productCellParser));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
