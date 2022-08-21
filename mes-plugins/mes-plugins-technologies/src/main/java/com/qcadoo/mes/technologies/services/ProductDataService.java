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
package com.qcadoo.mes.technologies.services;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ProductDataService {

    private class OperationConsumer implements Consumer<Entity> {

        private List<Entity> nodes = Lists.newArrayList();

        public void accept(final Entity node) {
            boolean showInProductData = node.getBooleanField("showInProductData");

            if (showInProductData) {
                nodes.add(node);
            }
        }

        public List<Entity> getNodes() {
            return nodes;
        }

    }

    public List<Entity> getOperations(final EntityTree operations) {
        OperationConsumer consumer = new OperationConsumer();

        operations.forEach(consumer);

        return consumer.getNodes();
    }

    public List<Entity> getInputs(final EntityTree operations) {
        OperationConsumer consumer = new OperationConsumer();

        operations.forEach(consumer);

        return operations
                .stream()
                .flatMap(
                        c -> {
                            List<Entity> operationProductInComponents = c
                                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
                            return operationProductInComponents.stream();
                        })
                .filter(c -> c.getBooleanField("showInProductData"))
                .map(c -> c
                        .getBelongsToField(OperationProductInComponentFields.PRODUCT))
                .collect(Collectors.toList());
    }

    public List<Entity> getOperationProductInComponents(final EntityTree operations) {
        OperationConsumer consumer = new OperationConsumer();

        operations.forEach(consumer);

        return operations
                .stream()
                .flatMap(
                        c -> {
                            List<Entity> operationProductInComponents = c
                                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
                            return operationProductInComponents.stream();
                        })
                .filter(c -> c.getBooleanField("showInProductData"))
                .collect(Collectors.toList());
    }

}
