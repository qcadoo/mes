package com.qcadoo.mes.materialFlowResources;

public class LocationDTO {

    private Long id;
    private boolean requirePrice;
    private boolean requirebatch;
    private boolean requirEproductionDate;
    private boolean requirEexpirationDate;
    private String algorithm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isRequirePrice() {
        return requirePrice;
    }

    public void setRequirePrice(boolean requirePrice) {
        this.requirePrice = requirePrice;
    }

    public boolean isRequirebatch() {
        return requirebatch;
    }

    public void setRequirebatch(boolean requirebatch) {
        this.requirebatch = requirebatch;
    }

    public boolean isRequirEproductionDate() {
        return requirEproductionDate;
    }

    public void setRequirEproductionDate(boolean requirEproductionDate) {
        this.requirEproductionDate = requirEproductionDate;
    }

    public boolean isRequirEexpirationDate() {
        return requirEexpirationDate;
    }

    public void setRequirEexpirationDate(boolean requirEexpirationDate) {
        this.requirEexpirationDate = requirEexpirationDate;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    
}
