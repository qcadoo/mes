package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class DocumentPositionDTO {

    private Long id;
    private Long document;
    private String product;
    private String additionalCode;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal givenquantity;
    private String givenunit;
    private BigDecimal conversion;
    private Date expirationdate;
    private Date productiondate;
    private String palletNumber;
    private String typeOfPallet;
    private String storageLocation;
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

    public String getAdditionalCode() {
        return additionalCode;
    }

    public void setAdditionalCode(String additionalCode) {
        this.additionalCode = additionalCode;
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

    public String getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
    }

    public String getTypeOfPallet() {
        return typeOfPallet;
    }

    public void setTypeOfPallet(String typeOfPallet) {
        this.typeOfPallet = typeOfPallet;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
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
        hash = 67 * hash + Objects.hashCode(this.additionalCode);
        hash = 67 * hash + Objects.hashCode(this.quantity);
        hash = 67 * hash + Objects.hashCode(this.unit);
        hash = 67 * hash + Objects.hashCode(this.givenquantity);
        hash = 67 * hash + Objects.hashCode(this.givenunit);
        hash = 67 * hash + Objects.hashCode(this.conversion);
        hash = 67 * hash + Objects.hashCode(this.expirationdate);
        hash = 67 * hash + Objects.hashCode(this.productiondate);
        hash = 67 * hash + Objects.hashCode(this.palletNumber);
        hash = 67 * hash + Objects.hashCode(this.typeOfPallet);
        hash = 67 * hash + Objects.hashCode(this.storageLocation);
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
        if (!Objects.equals(this.additionalCode, other.additionalCode)) {
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
        if (!Objects.equals(this.palletNumber, other.palletNumber)) {
            return false;
        }
        if (!Objects.equals(this.typeOfPallet, other.typeOfPallet)) {
            return false;
        }
        if (!Objects.equals(this.storageLocation, other.storageLocation)) {
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
        return "DocumentPositionDTO{" + "id=" + id + ", document=" + document + ", product=" + product + ", additional_code=" + additionalCode + ", quantity=" + quantity + ", unit=" + unit + ", givenquantity=" + givenquantity + ", givenunit=" + givenunit + ", conversion=" + conversion + ", expirationdate=" + expirationdate + ", productiondate=" + productiondate + ", pallet=" + palletNumber + ", type_of_pallet=" + typeOfPallet + ", storage_location=" + storageLocation + ", price=" + price + ", batch=" + batch + ", resource=" + resource + '}';
    }
    
    
}
