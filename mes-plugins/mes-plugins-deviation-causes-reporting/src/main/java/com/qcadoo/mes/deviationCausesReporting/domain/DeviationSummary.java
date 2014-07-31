package com.qcadoo.mes.deviationCausesReporting.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import com.google.common.base.Preconditions;

public class DeviationSummary implements DeviationCauseHolder {

    private final String deviationCause;

    private final LocalDate date;

    private final String orderNumber;

    private final String productNumber;

    private final String comment;

    public DeviationSummary(final String deviationCause, final LocalDate date, final String orderNumber,
            final String productNumber, final String comment) {
        Preconditions.checkArgument(deviationCause != null, "Deviation cause is mandatory!");
        this.deviationCause = deviationCause;
        this.date = date;
        this.orderNumber = orderNumber;
        this.productNumber = productNumber;
        this.comment = comment;
    }

    public String getDeviationCause() {
        return deviationCause;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        DeviationSummary rhs = (DeviationSummary) obj;
        return new EqualsBuilder().append(this.deviationCause, rhs.deviationCause).append(this.date, rhs.date)
                .append(this.orderNumber, rhs.orderNumber).append(this.productNumber, rhs.productNumber)
                .append(this.comment, rhs.comment).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deviationCause).append(date).append(orderNumber).append(productNumber)
                .append(comment).toHashCode();
    }

    @Override
    public String toString() {
        return String.format(
                "DeviationSummary[deviationCause=\"%s\", date=%s, orderNumber=\"%s\", productNumber=\"%s\", comment=\"%s\"]",
                deviationCause, date, orderNumber, productNumber, comment);
    }
}
