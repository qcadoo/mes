package com.qcadoo.mes.materialFlowMultitransfers.listeners;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCTS;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlowMultitransfers.constants.ProductQuantityFields.UNIT;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowMultitransfers.constants.MaterialFlowMultitransfersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
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
    private MaterialFlowService materialFlowService;

    // @Autowired
    // private MaterialFlowResourceService materialFlowResourceService;

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
        FieldComponent locationFromComponent = (FieldComponent) view.getComponentByReference(LOCATION_FROM);
        FieldComponent locationToComponent = (FieldComponent) view.getComponentByReference(LOCATION_TO);
        FieldComponent staffComponent = (FieldComponent) view.getComponentByReference(STAFF);

        typeComponent.requestComponentUpdateState();
        timeComponent.requestComponentUpdateState();
        locationToComponent.requestComponentUpdateState();
        locationFromComponent.requestComponentUpdateState();
        staffComponent.requestComponentUpdateState();

        String type = typeComponent.getFieldValue().toString();
        Date time = timeConverterService.getDateFromField(timeComponent.getFieldValue());
        Entity locationFrom = materialFlowService.getLocationById((Long) locationFromComponent.getFieldValue());
        Entity locationTo = materialFlowService.getLocationById((Long) locationToComponent.getFieldValue());
        Entity staff = materialFlowService.getStaffById((Long) staffComponent.getFieldValue());

        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        adlc.requestComponentUpdateState();

        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity productQuantity = formComponent.getEntity();
            BigDecimal quantity = productQuantity.getDecimalField(QUANTITY);
            Entity product = productQuantity.getBelongsToField(PRODUCT);

            if ((product != null) && (quantity != null)) {
                createTransfer(type, locationFrom, locationTo, product, quantity, staff, time);
            }
        }

        view.getComponentByReference(L_FORM).addMessage("materialFlow.multitransfer.generate.success", MessageType.SUCCESS);
    }

    public void getFromTemplate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        FieldComponent locationFromComponent = (FieldComponent) view.getComponentByReference(LOCATION_FROM);
        FieldComponent locationToComponent = (FieldComponent) view.getComponentByReference(LOCATION_TO);

        Entity locationFrom = materialFlowService.getLocationById((Long) locationFromComponent.getFieldValue());
        Entity locationTo = materialFlowService.getLocationById((Long) locationToComponent.getFieldValue());

        List<Entity> templates = getTransferTemplates(locationFrom, locationTo);

        if (templates.isEmpty()) {
            view.getComponentByReference(L_FORM).addMessage("materialFlow.multitransfer.template.failure", MessageType.FAILURE);
            return;
        }

        List<Entity> productQuantities = Lists.newArrayList();

        DataDefinition productQuantityDD = dataDefinitionService.get(MaterialFlowMultitransfersConstants.PLUGIN_IDENTIFIER,
                MaterialFlowMultitransfersConstants.MODEL_PRODUCT_QUANTITY);

        for (Entity template : templates) {
            Entity product = template.getBelongsToField(PRODUCT);

            Entity productQuantity = productQuantityDD.create();

            productQuantity.setField(PRODUCT, product);
            productQuantity.setField(UNIT, product.getStringField(UNIT));

            productQuantities.add(productQuantity);
        }

        adlc.setFieldValue(productQuantities);

        view.getComponentByReference(L_FORM).addMessage("materialFlow.multitransfer.template.success", MessageType.SUCCESS);
    }

    private boolean validateMultitransferForm(final ViewDefinitionState view) {
        boolean isValid = true;

        FieldComponent typeField = (FieldComponent) view.getComponentByReference(TYPE);
        FieldComponent timeField = (FieldComponent) view.getComponentByReference(TIME);
        FieldComponent locationFromField = (FieldComponent) view.getComponentByReference(LOCATION_FROM);
        FieldComponent locationToField = (FieldComponent) view.getComponentByReference(LOCATION_TO);

        if (typeField.getFieldValue() == null || typeField.getFieldValue().toString().isEmpty()) {
            typeField.addMessage("materialFlow.validate.global.error.fillType", MessageType.FAILURE);

            isValid = false;
        }
        if (timeField.getFieldValue() == null || timeField.getFieldValue().toString().isEmpty()) {
            timeField.addMessage("materialFlow.validate.global.error.fillDate", MessageType.FAILURE);

            isValid = false;
        }

        if ((locationFromField.getFieldValue() == null) && (locationToField.getFieldValue() == null)) {
            locationFromField.addMessage("materialFlow.validate.global.error.fillAtLeastOneLocation", MessageType.FAILURE);
            locationToField.addMessage("materialFlow.validate.global.error.fillAtLeastOneLocation", MessageType.FAILURE);

            isValid = false;
        }

        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(PRODUCTS);

        List<FormComponent> formComponents = adlc.getFormComponents();

        if (formComponents.isEmpty()) {
            view.getComponentByReference(L_FORM).addMessage("materialFlow.multitransfer.validation.productsAreRequired",
                    MessageType.FAILURE);

            isValid = false;
        } else {
            String type = (String) typeField.getFieldValue();
            Entity locationFrom = materialFlowService.getLocationById((Long) locationFromField.getFieldValue());

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

                // if ((type != null) && !PRODUCTION.getStringValue().equals(type) && (locationFrom != null) && (product != null)
                // && (quantity != null)
                // && !materialFlowResourceService.areResourcesSufficient(locationFrom, product, quantity)) {
                // formComponent.findFieldComponentByName(QUANTITY).addMessage(
                // "materialFlow.multitransfer.validation.resourcesArentSufficient", MessageType.FAILURE);
                //
                // isValid = false;
                // }
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

    private void createTransfer(final String type, final Entity locationFrom, final Entity locationTo, final Entity product,
            final BigDecimal quantity, final Entity staff, final Date time) {
        DataDefinition dd = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);

        Entity transfer = dd.create();
        String number = materialFlowService.generateNumberFromProduct(product, MaterialFlowConstants.MODEL_TRANSFER);

        transfer.setField(NUMBER, number);
        transfer.setField(TYPE, type);
        transfer.setField(TIME, time);
        transfer.setField(LOCATION_FROM, locationFrom);
        transfer.setField(LOCATION_TO, locationTo);
        transfer.setField(STAFF, staff);
        transfer.setField(PRODUCT, product);
        transfer.setField(QUANTITY, quantity);

        checkArgument(dd.save(transfer).isValid(), "invalid transfer id =" + transfer.getId());
    }

    private List<Entity> getTransferTemplates(final Entity locationFrom, final Entity locationTo) {
        return dataDefinitionService
                .get(MaterialFlowMultitransfersConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowMultitransfersConstants.MODEL_TRANSFER_TEMPLATE).find()
                .add(SearchRestrictions.belongsTo(LOCATION_FROM, locationFrom))
                .add(SearchRestrictions.belongsTo(LOCATION_TO, locationTo)).list().getEntities();
    }
}
