/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.validators.TechnologyTreeValidators;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OperationProductOutComponentHooks {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidators technologyTreeValidators;

    @Autowired
    private TranslationService translationService;

    public boolean validatesWith(final DataDefinition operationProductInComponentDD, final Entity operationProductInComponent) {
        boolean isValid = true;

        isValid = isValid && technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(operationProductInComponentDD,
                operationProductInComponent);
        isValid = isValid && technologyService.invalidateIfAlreadyInTheSameOperation(operationProductInComponentDD,
                operationProductInComponent);
        isValid = isValid && checkIfWasteProductsIsRightMarked(operationProductInComponentDD,
                operationProductInComponent);

        return isValid;
    }

    public void onSave(final DataDefinition operationProductOutComponentDD, final Entity operationProductOutComponent) {
        if (Objects.isNull(operationProductOutComponent.getField(OperationProductOutComponentFields.WASTE))) {
            operationProductOutComponent.setField(OperationProductOutComponentFields.WASTE, false);
        }
    }

    public boolean checkIfWasteProductsIsRightMarked(final DataDefinition operationProductInComponentDD, final Entity operationProductOutComponent) {

        Entity operationComponent = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);
        Entity technology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

        final EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        final EntityTreeNode root = operationComponents.getRoot();

        if(root.getId().equals(operationComponent.getId())) {
            Entity opocProduct = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);
            if(Objects.nonNull(opocProduct) && operationProductOutComponent.getBooleanField(OperationProductOutComponentFields.WASTE)
                && product.getId().equals(opocProduct.getId())) {
                operationProductOutComponent.addError(operationProductInComponentDD.getField(OperationProductOutComponentFields.WASTE), "technologies.technology.validate.global.error.theFinalProductCannotBeMarkedAsWaste");
                return false;
            }
            return true;
        }

        if (operationProductOutComponent.getBooleanField(OperationProductOutComponentFields.WASTE)) {
            return true;
        }

        long notWasteCount = getNotWasteCount(operationProductOutComponent);

        if (notWasteCount > 1) {
            operationProductOutComponent.addGlobalError(
                    "technologies.technology.validate.global.error.toManyNoWasteProductsInOperation");
            return false;
        }
        return true;
    }

    private long getNotWasteCount(Entity operationProductOutComponent) {
        Entity operationComponent = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);

        List<Entity> operationProductOutComponents = operationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);
        if (Objects.nonNull(operationProductOutComponent.getId())) {
            operationProductOutComponents = operationProductOutComponents.stream()
                    .filter(o -> !o.getId().equals(operationProductOutComponent.getId())).collect(Collectors.toList());
        }

        long notWasteCount = operationProductOutComponents.stream()
                .filter(opoc -> !opoc.getBooleanField(OperationProductOutComponentFields.WASTE)).count();

        notWasteCount = notWasteCount + 1;
        return notWasteCount;
    }

}
