package com.qcadoo.mes.deliveries.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class DeliveryDetailsListeners {

    public void setBufferForSupplier(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        // TODO ALBR implementation after SSTL when buffer field exists in company
    }
}
