package com.qcadoo.mes.materialFlowResources.constants;

import java.util.List;

public class PalletNumberProductRequest {
    private String palletNumber;
    private List<String> userLocationNumbers;

    public List<String> getUserLocationNumbers() {
        return userLocationNumbers;
    }

    public void setUserLocationNumbers(List<String> userLocationNumbers) {
        this.userLocationNumbers = userLocationNumbers;
    }

    public String getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
    }
}
