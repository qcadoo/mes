package com.qcadoo.mes.masterOrders.controllers.productBySize;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ProductsBySizeResponse {

    private SimpleResponseStatus status;

    private String message;

    public ProductsBySizeResponse() {
        super();
        this.status = SimpleResponseStatus.OK;
    }

    public ProductsBySizeResponse(final String message) {
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
        if (!super.equals(obj) || !(obj instanceof ProductsBySizeResponse)) {
            return false;
        }
        ProductsBySizeResponse other = (ProductsBySizeResponse) obj;
        return new EqualsBuilder().append(status, other.status).append(message, other.message).isEquals();
    }


}
