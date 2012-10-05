package com.qcadoo.mes.requestsForQuotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.requestsForQuotation.constans.RequestsForQuotationFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class RequestForQuotationService {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateCompanyNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, RequestsForQuotationFields.PLUGIN_IDENTIFIER,
                RequestsForQuotationFields.MODEL, RequestsForQuotationFields.L_FORM, "number");
    }

}
