/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basic.hooks;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PARTICULAR_PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;
import static com.qcadoo.mes.basic.constants.ProductFields.PARENT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.tree.ProductNumberingService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductHooks {

    @Autowired
    private ProductNumberingService productNumberingService;

    public void generateNodeNumber(final DataDefinition productDD, final Entity product) {
        productNumberingService.generateNodeNumber(product);
    }

    public void updateNodeNumber(final DataDefinition productDD, final Entity product) {
        productNumberingService.updateNodeNumber(product);
    }

    public void clearFamilyFromProductWhenTypeIsChanged(final DataDefinition productDD, final Entity product) {
        if (product.getId() == null) {
            return;
        }
        String entityType = product.getStringField(ENTITY_TYPE);
        Entity productFromDB = product.getDataDefinition().get(product.getId());
        if (entityType.equals(PARTICULAR_PRODUCT.getStringValue())
                && !entityType.equals(productFromDB.getStringField(ENTITY_TYPE))) {
            deleteProductFamily(productFromDB);
        }
    }

    private void deleteProductFamily(final Entity product) {
        DataDefinition productDD = product.getDataDefinition();
        List<Entity> productsWithFamily = productDD.find().add(SearchRestrictions.belongsTo(PARENT, product)).list()
                .getEntities();
        for (Entity entity : productsWithFamily) {
            entity.setField("parent", null);
            productDD.save(entity);
        }
    }

}
