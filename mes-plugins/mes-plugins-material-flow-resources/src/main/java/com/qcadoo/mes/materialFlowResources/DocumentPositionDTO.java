package com.qcadoo.mes.materialFlowResources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.SearchAttribute;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class DocumentPositionDTO {

    private Long id;

    private Long document;

    private Integer number;

    private String product;

    private String productName;

    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal quantity;

    @SearchAttribute(searchType = SearchAttribute.SEARCH_TYPE.EXACT_MATCH)
    private String unit;

    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal givenquantity;

    @SearchAttribute(searchType = SearchAttribute.SEARCH_TYPE.EXACT_MATCH)
    private String givenunit;

    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal conversion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date expirationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date productionDate;

    private String palletNumber;

    private String resourceNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date pickingDate;

    private String pickingWorker;

    @SearchAttribute(searchType = SearchAttribute.SEARCH_TYPE.EXACT_MATCH)
    private String typeOfLoadUnit;

    private String storageLocation;

    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal price;

    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal sellingPrice;

    private String batch;

    private Long batchId;

    private String resource;

    @JsonDeserialize(using = BooleanDeserializer.class)
    private Boolean waste;

    @JsonDeserialize(using = BooleanDeserializer.class)
    private Boolean lastResource;

    Map<String, Object> attrs = Maps.newHashMap();

    public DocumentPositionDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public String getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
    }

    public String getTypeOfLoadUnit() {
        return typeOfLoadUnit;
    }

    public void setTypeOfLoadUnit(String typeOfLoadUnit) {
        this.typeOfLoadUnit = typeOfLoadUnit;
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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Boolean isWaste() {
        return waste;
    }

    public void setWaste(Boolean waste) {
        this.waste = waste;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
        hash = 67 * hash + Objects.hashCode(this.document);
        hash = 67 * hash + Objects.hashCode(this.product);
        hash = 67 * hash + Objects.hashCode(this.quantity);
        hash = 67 * hash + Objects.hashCode(this.unit);
        hash = 67 * hash + Objects.hashCode(this.givenquantity);
        hash = 67 * hash + Objects.hashCode(this.givenunit);
        hash = 67 * hash + Objects.hashCode(this.conversion);
        hash = 67 * hash + Objects.hashCode(this.expirationDate);
        hash = 67 * hash + Objects.hashCode(this.productionDate);
        hash = 67 * hash + Objects.hashCode(this.palletNumber);
        hash = 67 * hash + Objects.hashCode(this.typeOfLoadUnit);
        hash = 67 * hash + Objects.hashCode(this.storageLocation);
        hash = 67 * hash + Objects.hashCode(this.price);
        hash = 67 * hash + Objects.hashCode(this.batch);
        hash = 67 * hash + Objects.hashCode(this.resource);
        hash = 67 * hash + Objects.hashCode(this.waste);
        hash = 67 * hash + Objects.hashCode(this.resourceNumber);
        hash = 67 * hash + Objects.hashCode(this.pickingDate);
        hash = 67 * hash + Objects.hashCode(this.pickingWorker);
        hash = 67 * hash + Objects.hashCode(this.sellingPrice);
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
        if (!Objects.equals(this.expirationDate, other.expirationDate)) {
            return false;
        }
        if (!Objects.equals(this.productionDate, other.productionDate)) {
            return false;
        }
        if (!Objects.equals(this.palletNumber, other.palletNumber)) {
            return false;
        }
        if (!Objects.equals(this.typeOfLoadUnit, other.typeOfLoadUnit)) {
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
        if (!Objects.equals(this.resourceNumber, other.resourceNumber)) {
            return false;
        }
        if (!Objects.equals(this.pickingDate, other.pickingDate)) {
            return false;
        }
        if (!Objects.equals(this.pickingWorker, other.pickingWorker)) {
            return false;
        }
        if (!Objects.equals(this.sellingPrice, other.sellingPrice)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DocumentPositionDTO{" + "id=" + id + ", document=" + document + ", product=" + product
                + ", quantity=" + quantity + ", unit=" + unit + ", givenquantity=" + givenquantity
                + ", givenunit=" + givenunit + ", conversion=" + conversion + ", expirationDate=" + expirationDate
                + ", productionDate=" + productionDate + ", pallet=" + palletNumber + ", type_of_loadUnit=" + typeOfLoadUnit
                + ", storage_location=" + storageLocation + ", price=" + price + ", batch=" + batch + ", resource=" + resource
                + ", resourceNumber=" + resourceNumber + ", pickingDate=" + pickingDate + ", pickingWorker=" + pickingWorker
                + '}';
    }

    public Boolean getLastResource() {
        return lastResource;
    }

    public void setLastResource(Boolean lastResource) {
        this.lastResource = lastResource;
    }

    public String getResourceNumber() {
        return resourceNumber;
    }

    public void setResourceNumber(String resourceNumber) {
        this.resourceNumber = resourceNumber;
    }

    public Date getPickingDate() {
        return pickingDate;
    }

    public void setPickingDate(Date pickingDate) {
        this.pickingDate = pickingDate;
    }

    public String getPickingWorker() {
        return pickingWorker;
    }

    public void setPickingWorker(String pickingWorker) {
        this.pickingWorker = pickingWorker;
    }
}
