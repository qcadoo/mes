/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.basicProductionCounting.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionCountingQuantityHooks {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void onCreate(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        fillOrder(productionCountingQuantity);
        fillOperationProductInComponent(productionCountingQuantity);
        fillOperationProductOutComponent(productionCountingQuantity);
        fillBasicProductionCounting(productionCountingQuantity);
        fillIsNonComponent(productionCountingQuantity);
    }

    public boolean onDelete(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        return deleteBasicProductionCounting(productionCountingQuantity);
    }

    private void fillOrder(final Entity productionCountingQuantity) {
        if (productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER) == null) {
            Entity basicProductionCounting = productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING);

            if (basicProductionCounting != null) {
                Entity order = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER);

                productionCountingQuantity.setField(ProductionCountingQuantityFields.ORDER, order);
            }
        }
    }

    private void fillOperationProductInComponent(final Entity productionCountingQuantity) {
        if (productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT) == null) {
            Entity technologyOperationComponent = productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
            String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

            if (checkIfShouldFillOperationProductInComponent(technologyOperationComponent, product, typeOfMaterial, role)) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT,
                        getOperationProductInComponent(technologyOperationComponent, product));
            }
        }
    }

    private boolean checkIfShouldFillOperationProductInComponent(final Entity technologyOperationComponent, final Entity product,
            final String typeOfMaterial, final String role) {
        return ((technologyOperationComponent != null) && (product != null) && !checkIfIsFinalProduct(typeOfMaterial) && checkIfIsUsed(role));
    }

    private Entity getOperationProductInComponent(final Entity technologyOperationComponent, final Entity product) {
        return technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)
                .find().add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
    }

    private void fillOperationProductOutComponent(final Entity productionCountingQuantity) {
        if (productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT) == null) {
            Entity technologyOperationComponent = productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
            String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

            if (checkIfShouldFillOperationProductOutComponent(technologyOperationComponent, product, typeOfMaterial, role)) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT,
                        getOperationProductOutComponent(technologyOperationComponent, product));
            }
        }
    }

    private boolean checkIfShouldFillOperationProductOutComponent(final Entity technologyOperationComponent,
            final Entity product, final String typeOfMaterial, final String role) {
        return ((technologyOperationComponent != null) && (product != null) && !checkIfIsFinalProduct(typeOfMaterial) && checkIfIsProduced(role));
    }

    private Entity getOperationProductOutComponent(final Entity technologyOperationComponent, final Entity product) {
        return technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS)
                .find().add(SearchRestrictions.belongsTo(OperationProductOutComponentFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
    }

    private void fillBasicProductionCounting(final Entity productionCountingQuantity) {
        if (productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING) == null) {
            Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
            String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

            if (checkIfShouldFillBasicProductionCounting(order, product, typeOfMaterial, role)) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING,
                        fillBasicProductionCounting(order, product));
            }
        }
    }

    private boolean checkIfShouldFillBasicProductionCounting(final Entity order, final Entity product,
            final String typeOfMaterial, final String role) {
        return ((order != null) && (product != null) && !checkIfBasicProductionCountingIsEmpty(order) && (checkIfIsUsed(role) || (checkIfIsProduced(role) && checkIfIsWaste(typeOfMaterial))));
    }

    private boolean checkIfBasicProductionCountingIsEmpty(final Entity order) {
        List<Entity> basicProductionCounting = order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS);

        return ((basicProductionCounting == null) || basicProductionCounting.isEmpty());
    }

    private Entity fillBasicProductionCounting(final Entity order, final Entity product) {
        Entity basicProductionCounting = getBasicProductionCounting(order, product);

        if (basicProductionCounting == null) {
            basicProductionCounting = basicProductionCountingService.createBasicProductionCounting(order, product);
        }

        return basicProductionCounting;
    }

    private Entity getBasicProductionCounting(final Entity order, final Entity product) {
        return order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS).find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
    }

    private void fillIsNonComponent(final Entity productionCountingQuantity) {
        if (productionCountingQuantity.getField(ProductionCountingQuantityFields.IS_NON_COMPONENT) == null) {
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

            boolean isNonComponent = true;

            if (checkIfIsFinalProduct(typeOfMaterial) || checkIfIsComponent(typeOfMaterial)) {
                isNonComponent = false;
            }

            productionCountingQuantity.setField(ProductionCountingQuantityFields.IS_NON_COMPONENT, isNonComponent);
        }
    }

    private boolean checkIfIsFinalProduct(final String typeOfMaterial) {
        return (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial));
    }

    private boolean checkIfIsComponent(final String typeOfMaterial) {
        return (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial));
    }

    private boolean checkIfIsWaste(final String typeOfMaterial) {
        return (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(typeOfMaterial));
    }

    private boolean checkIfIsUsed(final String role) {
        return (ProductionCountingQuantityRole.USED.getStringValue().equals(role));
    }

    private boolean checkIfIsProduced(final String role) {
        return (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role));
    }

    private boolean deleteBasicProductionCounting(final Entity productionCountingQuantity) {
        boolean isDeleted = true;

        Entity basicProductionCounting = productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING);

        if ((basicProductionCounting != null) && checkIfItIsLastProductionCountingQuantity(basicProductionCounting)) {
            productionCountingQuantity.setField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING, null);
            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

            isDeleted = basicProductionCounting.getDataDefinition().delete(basicProductionCounting.getId()).isSuccessfull();
        }

        return isDeleted;
    }

    private boolean checkIfItIsLastProductionCountingQuantity(final Entity basicProductionCounting) {
        return (basicProductionCounting.getHasManyField(BasicProductionCountingFields.PRODUCTION_COUNTING_QUANTITIES).size() == 1);
    }

}
