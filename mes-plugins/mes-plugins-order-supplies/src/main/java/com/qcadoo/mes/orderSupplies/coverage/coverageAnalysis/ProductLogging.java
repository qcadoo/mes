package com.qcadoo.mes.orderSupplies.coverage.coverageAnalysis;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class ProductLogging {

    private Long loggingId;

    private Long productId;

    private Date date;

    private BigDecimal reserveMissingQuantity;

    private BigDecimal changes;

    private String state;

    private String type;

    public Long getLoggingId() {
        return loggingId;
    }

    public void setLoggingId(Long loggingId) {
        this.loggingId = loggingId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getReserveMissingQuantity() {
        return reserveMissingQuantity;
    }

    public void setReserveMissingQuantity(BigDecimal reserveMissingQuantity) {
        this.reserveMissingQuantity = reserveMissingQuantity;
    }

    public BigDecimal getChanges() {
        return changes;
    }

    public void setChanges(BigDecimal changes) {
        this.changes = changes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductLogging that = (ProductLogging) o;
        return Objects.equals(loggingId, that.loggingId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loggingId, productId);
    }
}
