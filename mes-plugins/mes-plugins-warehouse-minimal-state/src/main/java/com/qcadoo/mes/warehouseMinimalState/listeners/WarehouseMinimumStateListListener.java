/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.warehouseMinimalState.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.warehouseMinimalState.constants.WarehouseMinimalStateConstants;
import com.qcadoo.mes.warehouseMinimalState.constants.WarehouseMinimumStateFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class WarehouseMinimumStateListListener {

    private static final String L_FORM = "form";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void redirectToAddManyMinimalState(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        Entity createMultiMinimalStateEntity = createMultiMinimalStateEntity();
        String url = "../page/warehouseMinimalState/warehouseMinimumStateAddMulti.html?context={\"form.id\":\""
                + createMultiMinimalStateEntity.getId() + "\"}";
        viewDefinitionState.openModal(url);
    }

    private Entity createMultiMinimalStateEntity() {
        Entity state = dataDefinitionService.get("warehouseMinimalState", "warehouseMinimumStateMulti").create();
        return state.getDataDefinition().save(state);
    }

    @Transactional
    public void createMultiMinimalStates(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity state = form.getPersistedEntityWithIncludedFormValues();

        if (state.getBelongsToField(WarehouseMinimumStateFields.LOCATION) == null) {
            LookupComponent location = (LookupComponent) view.getComponentByReference(WarehouseMinimumStateFields.LOCATION);
            location.addMessage(new ErrorMessage(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING));
            location.requestComponentUpdateState();
            return;
        }

        if (state.getManyToManyField("products") == null || state.getManyToManyField("products").isEmpty()) {
            view.addMessage(new ErrorMessage("warehouseMinimalState.warehouseMinimumStateAddMulti.error.productsEmpthy"));
            return;
        }
        state.getManyToManyField("products").forEach(p -> createMinimalStateEntity(state, p));
        componentState.addMessage("warehouseMinimalState.warehouseMinimumStateAddMulti.info.generated",
                ComponentState.MessageType.SUCCESS);

    }

    private void createMinimalStateEntity(Entity state, Entity product) {

        if (getLocationMinimumStateByProductAndLocation(
                dataDefinitionService.get("warehouseMinimalState", "warehouseMinimumState"), product,
                state.getBelongsToField(WarehouseMinimumStateFields.LOCATION)) == null) {

            Entity mstate = dataDefinitionService.get("warehouseMinimalState", "warehouseMinimumState").create();

            mstate.setField(WarehouseMinimumStateFields.PRODUCT, product);
            mstate.setField(WarehouseMinimumStateFields.LOCATION, state.getBelongsToField(WarehouseMinimumStateFields.LOCATION));
            mstate.setField(WarehouseMinimumStateFields.MINIMUM_STATE,
                    state.getDecimalField(WarehouseMinimumStateFields.MINIMUM_STATE));
            mstate.setField(WarehouseMinimumStateFields.OPTIMAL_ORDER_QUANTITY,
                    state.getDecimalField(WarehouseMinimumStateFields.OPTIMAL_ORDER_QUANTITY));
            mstate.getDataDefinition().save(mstate);
        }
    }

    private Entity getLocationMinimumStateByProductAndLocation(final DataDefinition locationMinimumStateDD, final Entity product,
            final Entity location) {
        return locationMinimumStateDD.find().add(SearchRestrictions.belongsTo(WarehouseMinimumStateFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(WarehouseMinimumStateFields.LOCATION, location)).setMaxResults(1)
                .uniqueResult();
    }

    public void printDocument(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

        viewDefinitionState.redirectTo("/" + WarehouseMinimalStateConstants.PLUGIN_IDENTIFIER + "/document." + args[0], true,
                false);
    }
}
