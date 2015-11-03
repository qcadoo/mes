package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;

public class DocumentPositionVO {

    private Long id;

    private BigDecimal quantity;

    private BigDecimal givenquantity;

    private Date expirationdate;

    private String type;

    private Long product_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getGivenquantity() {
        return givenquantity;
    }

    public void setGivenquantity(BigDecimal givenquantity) {
        this.givenquantity = givenquantity;
    }

    public Date getExpirationdate() {
        return expirationdate;
    }

    public void setExpirationdate(Date expirationdate) {
        this.expirationdate = expirationdate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }
}
