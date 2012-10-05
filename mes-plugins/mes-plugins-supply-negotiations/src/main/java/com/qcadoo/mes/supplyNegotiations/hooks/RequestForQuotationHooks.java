package com.qcadoo.mes.supplyNegotiations.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class RequestForQuotationHooks {

    public void setBufferForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(RequestForQuotationFields.SUPPLIER);
        FieldComponent deliveryDateBuffer = (FieldComponent) view.getComponentByReference("deliveryDateBuffer");
        Entity supplier = supplierLookup.getEntity();
        if (supplier == null) {
            deliveryDateBuffer.setFieldValue(null);
        } else {
            deliveryDateBuffer.setFieldValue(supplier.getField("buffer"));
        }
        deliveryDateBuffer.requestComponentUpdateState();
    }

}
