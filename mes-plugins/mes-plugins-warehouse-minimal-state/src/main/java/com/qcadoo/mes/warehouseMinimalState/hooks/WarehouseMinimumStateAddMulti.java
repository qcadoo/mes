package com.qcadoo.mes.warehouseMinimalState.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.stereotype.Service;

@Service
public class WarehouseMinimumStateAddMulti {

    public void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent location = (LookupComponent) view.getComponentByReference("location");
        location.setRequired(true);
    }
}
