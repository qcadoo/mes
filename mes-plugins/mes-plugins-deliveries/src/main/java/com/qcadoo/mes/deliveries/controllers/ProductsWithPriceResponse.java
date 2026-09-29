package com.qcadoo.mes.deliveries.controllers;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ProductsWithPriceResponse {

    private SimpleResponseStatus status;

    private String message;

    public ProductsWithPriceResponse() {
        super();
        this.status = SimpleResponseStatus.OK;
    }

    public ProductsWithPriceResponse(final String message) {
        super();
        this.status = SimpleResponseStatus.ERROR;
        this.message = message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setStatus(final SimpleResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public SimpleResponseStatus getStatus() {
        return status;
    }

    public enum SimpleResponseStatus {
        OK, ERROR;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(message).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof ProductsWithPriceResponse)) {
            return false;
        }
        ProductsWithPriceResponse other = (ProductsWithPriceResponse) obj;
        return new EqualsBuilder().append(status, other.status).append(message, other.message).isEquals();
    }


}
