package com.qcadoo.mes.materialFlow.listeners;

import static com.qcadoo.mes.materialFlow.constants.MultitransferViewComponents.PRODUCTS;
import static com.qcadoo.mes.materialFlow.constants.MultitransferViewComponents.STAFF;
import static com.qcadoo.mes.materialFlow.constants.MultitransferViewComponents.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.MultitransferViewComponents.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.MultitransferViewComponents.TIME;
import static com.qcadoo.mes.materialFlow.constants.MultitransferViewComponents.TYPE;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlow.MaterialFlowTransferService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.TimeConverterService;

@Component
public class MultitransferListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialFlowTransferService materialFlowTransferService;

    @Autowired
    private TimeConverterService timeConverterService;

    public void fillUnitsInADL(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);
        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            Entity product = productQuantity.getBelongsToField("product");
            if (product != null) {
                productQuantity.setField("unit", product.getStringField("unit"));
            }
            formComponent.setEntity(productQuantity);
        }
    }

    @Transactional
    public void createMultitransfer(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (!validateMultitransferForm(view)) {
            return;
        }

        FieldComponent stockAreaToComponent = (FieldComponent) view.getComponentByReference(STOCK_AREAS_TO);
        FieldComponent stockAreaFromComponent = (FieldComponent) view.getComponentByReference(STOCK_AREAS_FROM);
        FieldComponent typeComponent = (FieldComponent) view.getComponentByReference(TYPE);
        FieldComponent timeComponent = (FieldComponent) view.getComponentByReference(TIME);
        FieldComponent staffComponent = (FieldComponent) view.getComponentByReference(STAFF);

        String type = typeComponent.getFieldValue().toString();

        Entity stockAreaFrom = getAreaById((Long) stockAreaFromComponent.getFieldValue());
        Entity stockAreaTo = getAreaById((Long) stockAreaToComponent.getFieldValue());
        Entity staff = getStaffById((Long) staffComponent.getFieldValue());
        Date time = timeConverterService.getDateFromField(timeComponent.getFieldValue());
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            BigDecimal quantity = productQuantity.getDecimalField("quantity");
            Entity product = productQuantity.getBelongsToField("product");
            if (product != null) {
                materialFlowTransferService.createTransfer(type, stockAreaFrom, stockAreaTo, product, quantity, staff, time);
            }
        }

        view.getComponentByReference("form").addMessage("materialFlow.multitransfer.generate.success", MessageType.SUCCESS);
    }

    public void getFromTemplate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);
        FieldComponent stockAreaToComponent = (FieldComponent) view.getComponentByReference(STOCK_AREAS_TO);
        FieldComponent stockAreaFromComponent = (FieldComponent) view.getComponentByReference(STOCK_AREAS_FROM);
        Entity stockAreaFrom = getAreaById((Long) stockAreaFromComponent.getFieldValue());
        Entity stockAreaTo = getAreaById((Long) stockAreaToComponent.getFieldValue());

        List<Entity> templates = materialFlowTransferService.getTransferTemplates(stockAreaFrom, stockAreaTo);

        if (templates.isEmpty()) {
            view.getComponentByReference("form").addMessage("materialFlow.multitransfer.template.failure", MessageType.FAILURE);
            return;
        }

        List<Entity> productQuantities = Lists.newArrayList();

        DataDefinition productQuantityDD = dataDefinitionService.get("materialFlow", "productQuantity");

        for (Entity template : templates) {
            Entity productQuantity = productQuantityDD.create();
            Entity product = template.getBelongsToField("product");
            productQuantity.setField("product", product);
            productQuantity.setField("unit", product.getStringField("unit"));
            productQuantities.add(productQuantity);
        }

        adlc.setFieldValue(productQuantities);

        view.getComponentByReference("form").addMessage("materialFlow.multitransfer.template.success", MessageType.SUCCESS);
    }

    private Entity getAreaById(final Long id) {
        if (id == null) {
            return null;
        }
        DataDefinition dd = dataDefinitionService.get("materialFlow", "stockAreas");
        return dd.get(id);
    }

    private Entity getStaffById(final Long id) {
        if (id == null) {
            return null;
        }
        DataDefinition dd = dataDefinitionService.get("basic", "staff");
        return dd.get(id);
    }

    private boolean validateMultitransferForm(final ViewDefinitionState view) {
        boolean isValid = true;

        for (String componentRef : Arrays.asList(STOCK_AREAS_TO, STOCK_AREAS_FROM, TYPE, TIME)) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(componentRef);
            if (component.isRequired()) {
                Object fieldValue = component.getFieldValue();
                if (fieldValue == null || fieldValue.toString().isEmpty()) {
                    component.addMessage("materialFlow.multitransfer.validation.fieldRequired", MessageType.FAILURE);
                    isValid = false;
                }
            }
        }

        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        List<FormComponent> formComponents = adlc.getFormComponents();

        if (formComponents.isEmpty()) {
            isValid = false;
            view.getComponentByReference("form").addMessage("materialFlow.multitransfer.validation.productsAreRequired",
                    MessageType.FAILURE);
        }

        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            BigDecimal quantity = productQuantity.getDecimalField("quantity");
            Entity product = productQuantity.getBelongsToField("product");
            if (product == null) {
                formComponent.findFieldComponentByName("product").addMessage(
                        "materialFlow.multitransfer.validation.fieldRequired", MessageType.FAILURE);
                isValid = false;
            }
            if (quantity == null) {
                formComponent.findFieldComponentByName("quantity").addMessage(
                        "materialFlow.multitransfer.validation.fieldRequired", MessageType.FAILURE);
                isValid = false;
            }
        }

        return isValid;
    }
}
