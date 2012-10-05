package com.qcadoo.mes.supplyNegotiations.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class RequestForQuotationHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void setBufferForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(RequestForQuotationFields.SUPPLIER);
        FieldComponent deliveryDateBuffer = (FieldComponent) view
                .getComponentByReference(RequestForQuotationFields.DELIVERY_DATE_BUFFER);
        Entity supplier = supplierLookup.getEntity();
        if (supplier == null) {
            deliveryDateBuffer.setFieldValue(null);
        } else {
            deliveryDateBuffer.setFieldValue(supplier.getField(RequestForQuotationFields.BUFFER));
        }
        deliveryDateBuffer.requestComponentUpdateState();
    }

    public void generateCompanyNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION, "form", RequestForQuotationFields.NUMBER);
    }

}
