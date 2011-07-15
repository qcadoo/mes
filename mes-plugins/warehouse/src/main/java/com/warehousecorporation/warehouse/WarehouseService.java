package com.warehousecorporation.warehouse;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class WarehouseService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    public void setWorkersDatesAndResourceQuantity(final DataDefinition dataDefinition, final Entity transfer) {
        if (transfer.getId() == null) {
            transfer.setField("requestWorker", securityService.getCurrentUserName());
            transfer.setField("requestDate", new Date());
        }
        if ("closed".equals(transfer.getField("status"))) {
            transfer.setField("confirmWorker", securityService.getCurrentUserName());
            transfer.setField("confirmDate", new Date());

            DataDefinition resourceDataDefinition = dataDefinitionService.get("warehouse", "resource");

            Entity resource = transfer.getBelongsToField("resource");

            BigDecimal currentQuantity = (BigDecimal) resource.getField("quantity");
            BigDecimal transferQuantity = (BigDecimal) transfer.getField("quantity");
            BigDecimal newQuantity;

            if ("delivery".equals(transfer.getField("type"))) {
                newQuantity = new BigDecimal(currentQuantity.doubleValue() - transferQuantity.doubleValue());
            } else {
                newQuantity = new BigDecimal(currentQuantity.doubleValue() + transferQuantity.doubleValue());
            }

            if (newQuantity.doubleValue() >= 0) {
                resource.setField("quantity", newQuantity);
                resourceDataDefinition.save(resource);
            }
        }
    }

    public boolean checkIfHasEnoughtQuantity(final DataDefinition dataDefinition, final Entity transfer) {
        if ("closed".equals(transfer.getField("status")) && "delivery".equals(transfer.getField("type"))) {

            Entity resource = transfer.getBelongsToField("resource");

            BigDecimal currentQuantity = (BigDecimal) resource.getField("quantity");
            BigDecimal transferQuantity = (BigDecimal) transfer.getField("quantity");

            if (transferQuantity.compareTo(currentQuantity) > 0) {
                transfer.addError(dataDefinition.getField("quantity"), "warehouse.not.enought.resource.error");
                return false;
            }
        }

        return true;
    }

    public void setResourceInitialQuantity(final ViewDefinitionState state) {

        ComponentState quantity = (ComponentState) state.getComponentByReference("quantity");
        if(quantity.getFieldValue() == null) {
            quantity.setFieldValue(0);
        }
    }
}