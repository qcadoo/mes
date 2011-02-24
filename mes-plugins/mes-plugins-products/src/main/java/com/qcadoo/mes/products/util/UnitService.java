package com.qcadoo.mes.products.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.lookup.LookupComponentState;

@Service
public class UnitService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillProductUnit(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        fillProductUnitPreRender(viewDefinitionState, viewDefinitionState.getLocale());
    }

    public void fillProductUnitPreRender(final ViewDefinitionState state, final Locale locale) {
        LookupComponentState productState = (LookupComponentState) state.getComponentByReference("product");
        FieldComponentState unitState = (FieldComponentState) state.getComponentByReference("unit");
        unitState.requestComponentUpdateState();
        if (productState.getFieldValue() != null) {
            Entity product = dataDefinitionService.get("products", "product").get(productState.getFieldValue());
            unitState.setFieldValue(product.getStringField("unit"));
        } else {
            unitState.setFieldValue("");
        }
    }
}
