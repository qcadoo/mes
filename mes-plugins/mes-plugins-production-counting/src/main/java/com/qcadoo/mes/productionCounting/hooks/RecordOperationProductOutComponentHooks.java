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
package com.qcadoo.mes.productionCounting.hooks;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.listeners.RecordOperationProductComponentListeners;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class RecordOperationProductOutComponentHooks {

    @Autowired
    private RecordOperationProductComponentListeners recordOperationProductComponentListeners;

    private static final String L_FORM = "form";

    public void onBeforeRender(final ViewDefinitionState view) {
        recordOperationProductComponentListeners.onBeforeRender(view);
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity componentEntity = form.getPersistedEntityWithIncludedFormValues();

        hideSetTab(view, componentEntity);
    }

    private void hideSetTab(ViewDefinitionState view, Entity componentEntity) {
        boolean isSet = false;
        Entity productionTracking = componentEntity
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
        Entity product = componentEntity.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        EntityList operationProductOutComponents = technologyOperationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

        Optional<Entity> operationProductOutComponent = operationProductOutComponents
                .stream()
                .filter(opoc -> opoc.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId()
                        .equals(product.getId())).findFirst();
        if(operationProductOutComponent.isPresent()) {
            isSet = operationProductOutComponent.get().getBooleanField("set");
        }
        if(!isSet) {
            view.getComponentByReference("setTab").setVisible(false);
        } else {
            view.getComponentByReference("setTab").setVisible(true);
        }
    }
}
