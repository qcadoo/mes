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
package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.RepackingFields;
import com.qcadoo.mes.materialFlowResources.states.RepackingServiceMarker;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RepackingDetailsListeners {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private StateExecutorService stateExecutorService;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(RepackingServiceMarker.class, view, args);
    }

    public void fillTypeOfLoadUnitField(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(RepackingFields.LOCATION);
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(RepackingFields.PALLET_NUMBER);
        LookupComponent typeOfLoadUnitLookup = (LookupComponent) view.getComponentByReference(RepackingFields.TYPE_OF_LOAD_UNIT);

        Entity location = locationLookup.getEntity();
        Entity palletNumber = palletNumberLookup.getEntity();
        Long typeOfLoadUnit = null;

        if (Objects.nonNull(location) && Objects.nonNull(palletNumber)) {
            typeOfLoadUnit = materialFlowResourcesService.getTypeOfLoadUnitByPalletNumber(location.getId(), palletNumber.getStringField(PalletNumberFields.NUMBER));
        }

        typeOfLoadUnitLookup.setFieldValue(typeOfLoadUnit);
        typeOfLoadUnitLookup.requestComponentUpdateState();
    }

    public void clearStorageLocationAndPalletNumber(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(RepackingFields.STORAGE_LOCATION);
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(RepackingFields.PALLET_NUMBER);
        storageLocationLookup.setFieldValue(null);
        storageLocationLookup.requestComponentUpdateState();
        palletNumberLookup.setFieldValue(null);
        palletNumberLookup.requestComponentUpdateState();

    }

}
