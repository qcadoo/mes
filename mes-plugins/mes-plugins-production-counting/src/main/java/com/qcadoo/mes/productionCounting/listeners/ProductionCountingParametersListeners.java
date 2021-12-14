package com.qcadoo.mes.productionCounting.listeners;

import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.PriceBasedOn;
import com.qcadoo.mes.productionCounting.constants.ReceiptOfProducts;
import com.qcadoo.mes.productionCounting.constants.ReleaseOfMaterials;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.stereotype.Service;

@Service
public class ProductionCountingParametersListeners {

    /**
     * Cena PW na podst.:
     *
     * parametr aktywny, gdy Przyjęcie wyrobów = na zakończeniu zlecenia. Wówczas użytkownik może wybrać, czy chce przyjmować wg kosztu nominalnego, czy rzeczywistego TKW
     *
     * gdy Przyjęcie wyrobów <> na zakończeniu zlecenia, to parametr ustawiony jako Koszt nominalny produktu i nie można go zmienić
     */
    public void onReceiptOfProductsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent priceBasedOn = (FieldComponent) view.getComponentByReference(ParameterFieldsPC.PRICE_BASED_ON);
        FieldComponent receiptOfProducts = (FieldComponent) view.getComponentByReference(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);
        if (ReceiptOfProducts.END_OF_THE_ORDER.getStringValue()
                .equals(receiptOfProducts.getFieldValue().toString())) {
            priceBasedOn.setEnabled(true);
        } else {
            priceBasedOn.setEnabled(false);
            priceBasedOn.setFieldValue(PriceBasedOn.NOMINAL_PRODUCT_COST.getStringValue());
        }
    }

    public void onReleaseOfMaterialsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent consumptionOfRawMaterialsBasedOnStandards = (CheckBoxComponent) view
                .getComponentByReference(ParameterFieldsPC.CONSUMPTION_OF_RAW_MATERIALS_BASED_ON_STANDARDS);

        FieldComponent releaseOfMaterials = (FieldComponent) view.getComponentByReference(ParameterFieldsPC.RELEASE_OF_MATERIALS);
        if (ReleaseOfMaterials.MANUALLY_TO_ORDER_OR_GROUP.getStringValue()
                .equals(releaseOfMaterials.getFieldValue().toString())) {
            consumptionOfRawMaterialsBasedOnStandards.setChecked(false);
            consumptionOfRawMaterialsBasedOnStandards.setEnabled(false);
        } else {
            consumptionOfRawMaterialsBasedOnStandards.setEnabled(true);
        }

    }
}
