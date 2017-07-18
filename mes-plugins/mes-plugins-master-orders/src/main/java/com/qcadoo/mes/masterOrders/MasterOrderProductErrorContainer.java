package com.qcadoo.mes.masterOrders;

import com.qcadoo.model.api.validators.ErrorMessage;

import java.math.BigDecimal;
import java.util.List;

public class MasterOrderProductErrorContainer {

    private BigDecimal quantity;
    private String product;
    private String masterOrder;
    private List<ErrorMessage> errorMessages;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getMasterOrder() {
        return masterOrder;
    }

    public void setMasterOrder(String masterOrder) {
        this.masterOrder = masterOrder;
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
