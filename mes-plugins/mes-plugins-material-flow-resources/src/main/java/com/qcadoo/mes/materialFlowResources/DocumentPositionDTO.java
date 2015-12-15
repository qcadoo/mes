package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class DocumentPositionDTO {

    private Long id;
    private Long document;
    private String product;
    private String additional_code;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal givenquantity;
    private String givenunit;
    private BigDecimal conversion;
    private Date expirationdate;
    private Date productiondate;
    private String pallet;
    private String type_of_pallet;
    private String storage_location;
    private BigDecimal price;
    private String batch;
    // TODO
    private Long resource;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocument() {
        return document;
    }

    public void setDocument(Long document) {
        this.document = document;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAdditional_code() {
        return additional_code;
    }

    public void setAdditional_code(String additional_code) {
        this.additional_code = additional_code;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getGivenquantity() {
        return givenquantity;
    }

    public void setGivenquantity(BigDecimal givenquantity) {
        this.givenquantity = givenquantity;
    }

    public String getGivenunit() {
        return givenunit;
    }

    public void setGivenunit(String givenunit) {
        this.givenunit = givenunit;
    }

    public BigDecimal getConversion() {
        return conversion;
    }

    public void setConversion(BigDecimal conversion) {
        this.conversion = conversion;
    }

    public Date getExpirationdate() {
        return expirationdate;
    }

    public void setExpirationdate(Date expirationdate) {
        this.expirationdate = expirationdate;
    }

    public Date getProductiondate() {
        return productiondate;
    }

    public void setProductiondate(Date productiondate) {
        this.productiondate = productiondate;
    }

    public String getPallet() {
        return pallet;
    }

    public void setPallet(String pallet) {
        this.pallet = pallet;
    }

    public String getType_of_pallet() {
        return type_of_pallet;
    }

    public void setType_of_pallet(String type_of_pallet) {
        this.type_of_pallet = type_of_pallet;
    }

    public String getStorage_location() {
        return storage_location;
    }

    public void setStorage_location(String storage_location) {
        this.storage_location = storage_location;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public Long getResource() {
        return resource;
    }

    public void setResource(Long resource) {
        this.resource = resource;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
        hash = 67 * hash + Objects.hashCode(this.document);
        hash = 67 * hash + Objects.hashCode(this.product);
        hash = 67 * hash + Objects.hashCode(this.additional_code);
        hash = 67 * hash + Objects.hashCode(this.quantity);
        hash = 67 * hash + Objects.hashCode(this.unit);
        hash = 67 * hash + Objects.hashCode(this.givenquantity);
        hash = 67 * hash + Objects.hashCode(this.givenunit);
        hash = 67 * hash + Objects.hashCode(this.conversion);
        hash = 67 * hash + Objects.hashCode(this.expirationdate);
        hash = 67 * hash + Objects.hashCode(this.productiondate);
        hash = 67 * hash + Objects.hashCode(this.pallet);
        hash = 67 * hash + Objects.hashCode(this.type_of_pallet);
        hash = 67 * hash + Objects.hashCode(this.storage_location);
        hash = 67 * hash + Objects.hashCode(this.price);
        hash = 67 * hash + Objects.hashCode(this.batch);
        hash = 67 * hash + Objects.hashCode(this.resource);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentPositionDTO other = (DocumentPositionDTO) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.document, other.document)) {
            return false;
        }
        if (!Objects.equals(this.product, other.product)) {
            return false;
        }
        if (!Objects.equals(this.additional_code, other.additional_code)) {
            return false;
        }
        if (!Objects.equals(this.quantity, other.quantity)) {
            return false;
        }
        if (!Objects.equals(this.unit, other.unit)) {
            return false;
        }
        if (!Objects.equals(this.givenquantity, other.givenquantity)) {
            return false;
        }
        if (!Objects.equals(this.givenunit, other.givenunit)) {
            return false;
        }
        if (!Objects.equals(this.conversion, other.conversion)) {
            return false;
        }
        if (!Objects.equals(this.expirationdate, other.expirationdate)) {
            return false;
        }
        if (!Objects.equals(this.productiondate, other.productiondate)) {
            return false;
        }
        if (!Objects.equals(this.pallet, other.pallet)) {
            return false;
        }
        if (!Objects.equals(this.type_of_pallet, other.type_of_pallet)) {
            return false;
        }
        if (!Objects.equals(this.storage_location, other.storage_location)) {
            return false;
        }
        if (!Objects.equals(this.price, other.price)) {
            return false;
        }
        if (!Objects.equals(this.batch, other.batch)) {
            return false;
        }
        if (!Objects.equals(this.resource, other.resource)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DocumentPositionDTO{" + "id=" + id + ", document=" + document + ", product=" + product + ", additional_code=" + additional_code + ", quantity=" + quantity + ", unit=" + unit + ", givenquantity=" + givenquantity + ", givenunit=" + givenunit + ", conversion=" + conversion + ", expirationdate=" + expirationdate + ", productiondate=" + productiondate + ", pallet=" + pallet + ", type_of_pallet=" + type_of_pallet + ", storage_location=" + storage_location + ", price=" + price + ", batch=" + batch + ", resource=" + resource + '}';
    }
    
    
}
