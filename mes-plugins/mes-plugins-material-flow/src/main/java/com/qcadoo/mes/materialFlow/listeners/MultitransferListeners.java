package com.qcadoo.mes.materialFlow.listeners;

import static com.qcadoo.mes.materialFlow.constants.ProductQuantityFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.ProductQuantityFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.ProductQuantityFields.UNIT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCTS;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STOCK_AREAS_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.TRANSPORT;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlow.MaterialFlowResourceService;
import com.qcadoo.mes.materialFlow.MaterialFlowTransferService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
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

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialFlowTransferService materialFlowTransferService;

    @Autowired
    private MaterialFlowResourceService materialFlowResourceService;

    @Autowired
    private TimeConverterService timeConverterService;

    public void fillUnitsInADL(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        fillUnitsInADL(view, PRODUCTS);
    }

    private void fillUnitsInADL(final ViewDefinitionState view, final String adlName) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(adlName);
        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity entity = formComponent.getEntity();
            Entity product = entity.getBelongsToField(PRODUCT);
            if (product != null) {
                entity.setField(UNIT, product.getStringField(UNIT));
            }
            formComponent.setEntity(entity);
        }
    }

    @Transactional
    public void createMultitransfer(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (!validateMultitransferForm(view)) {
            return;
        }

        FieldComponent typeComponent = (FieldComponent) view.getComponentByReference(TYPE);
        FieldComponent timeComponent = (FieldComponent) view.getComponentByReference(TIME);
        FieldComponent stockAreaToComponent = (FieldComponent) view.getComponentByReference(STOCK_AREAS_TO);
        FieldComponent stockAreaFromComponent = (FieldComponent) view.getComponentByReference(STOCK_AREAS_FROM);
        FieldComponent staffComponent = (FieldComponent) view.getComponentByReference(STAFF);

        typeComponent.requestComponentUpdateState();
        timeComponent.requestComponentUpdateState();
        stockAreaToComponent.requestComponentUpdateState();
        stockAreaFromComponent.requestComponentUpdateState();
        staffComponent.requestComponentUpdateState();

        String type = typeComponent.getFieldValue().toString();

        Entity stockAreaFrom = getAreaById((Long) stockAreaFromComponent.getFieldValue());
        Entity stockAreaTo = getAreaById((Long) stockAreaToComponent.getFieldValue());
        Entity staff = getStaffById((Long) staffComponent.getFieldValue());
        Date time = timeConverterService.getDateFromField(timeComponent.getFieldValue());
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        adlc.requestComponentUpdateState();

        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            BigDecimal quantity = productQuantity.getDecimalField(QUANTITY);
            Entity product = productQuantity.getBelongsToField(PRODUCT);

            if (product != null) {
                materialFlowTransferService.createTransfer(type, stockAreaFrom, stockAreaTo, product, quantity, staff, time);
            }
        }

        view.getComponentByReference(L_FORM).addMessage("materialFlow.multitransfer.generate.success", MessageType.SUCCESS);
    }

    public void getFromTemplate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);
        FieldComponent stockAreaToComponent = (FieldComponent) view.getComponentByReference(STOCK_AREAS_TO);
        FieldComponent stockAreaFromComponent = (FieldComponent) view.getComponentByReference(STOCK_AREAS_FROM);
        Entity stockAreaFrom = getAreaById((Long) stockAreaFromComponent.getFieldValue());
        Entity stockAreaTo = getAreaById((Long) stockAreaToComponent.getFieldValue());

        List<Entity> templates = materialFlowTransferService.getTransferTemplates(stockAreaFrom, stockAreaTo);

        if (templates.isEmpty()) {
            view.getComponentByReference(L_FORM).addMessage("materialFlow.multitransfer.template.failure", MessageType.FAILURE);
            return;
        }

        List<Entity> productQuantities = Lists.newArrayList();

        DataDefinition productQuantityDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_PRODUCT_QUANTITY);

        for (Entity template : templates) {
            Entity productQuantity = productQuantityDD.create();
            Entity product = template.getBelongsToField(PRODUCT);
            productQuantity.setField(PRODUCT, product);
            productQuantity.setField(UNIT, product.getStringField(UNIT));
            productQuantities.add(productQuantity);
        }

        adlc.setFieldValue(productQuantities);

        view.getComponentByReference(L_FORM).addMessage("materialFlow.multitransfer.template.success", MessageType.SUCCESS);
    }

    private Entity getAreaById(final Long id) {
        if (id == null) {
            return null;
        }
        DataDefinition dd = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_STOCK_AREAS);
        return dd.get(id);
    }

    private Entity getStaffById(final Long id) {
        if (id == null) {
            return null;
        }
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF);
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
            view.getComponentByReference(L_FORM).addMessage("materialFlow.multitransfer.validation.productsAreRequired",
                    MessageType.FAILURE);

            isValid = false;
        } else {
            FieldComponent typeField = (FieldComponent) view.getComponentByReference(TYPE);
            FieldComponent stockAreasFromField = (FieldComponent) view.getComponentByReference(STOCK_AREAS_FROM);

            String type = null;
            Entity stockAreasFrom = null;

            if ((typeField != null) && (stockAreasFromField != null)) {
                type = (String) typeField.getFieldValue();
                stockAreasFrom = getAreaById((Long) stockAreasFromField.getFieldValue());
            }

            for (FormComponent formComponent : formComponents) {
                Entity productQuantity = formComponent.getEntity();
                BigDecimal quantity = productQuantity.getDecimalField(QUANTITY);
                Entity product = productQuantity.getBelongsToField(PRODUCT);

                if (product == null) {
                    formComponent.findFieldComponentByName(PRODUCT).addMessage(
                            "materialFlow.multitransfer.validation.fieldRequired", MessageType.FAILURE);

                    isValid = false;
                } else {
                    if (isProductAlreadyAdded(formComponents, product)) {
                        formComponent.findFieldComponentByName(PRODUCT).addMessage(
                                "materialFlow.multitransfer.validation.productAlreadyAdded", MessageType.FAILURE);

                        isValid = false;
                    }
                }

                if (quantity == null) {
                    formComponent.findFieldComponentByName(QUANTITY).addMessage(
                            "materialFlow.multitransfer.validation.fieldRequired", MessageType.FAILURE);

                    isValid = false;
                }

                if ((type != null) && (CONSUMPTION.getStringValue().equals(type) || TRANSPORT.getStringValue().equals(type))
                        && (stockAreasFrom != null) && (product != null) && (quantity != null)
                        && !materialFlowResourceService.areResourcesSufficient(stockAreasFrom, product, quantity)) {
                    formComponent.findFieldComponentByName(QUANTITY).addMessage(
                            "materialFlow.multitransfer.validation.resourcesArentSufficient", MessageType.FAILURE);

                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean isProductAlreadyAdded(final List<FormComponent> formComponents, final Entity product) {
        if (product == null) {
            return false;
        }
        int count = 0;
        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            Entity productAlreadyAdded = productQuantity.getBelongsToField(PRODUCT);

            if (product.equals(productAlreadyAdded)) {
                count++;
                if (count > 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
