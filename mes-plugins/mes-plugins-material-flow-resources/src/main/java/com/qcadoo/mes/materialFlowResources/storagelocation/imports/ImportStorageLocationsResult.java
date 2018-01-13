package com.qcadoo.mes.materialFlowResources.storagelocation.imports;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ImportStorageLocationsResult {

    List<String> notExistingProducts = Lists.newArrayList();

    boolean imported = true;

    void addNotExcitingProduct(String productNumber) {
        if(StringUtils.isNotEmpty(productNumber)) {
            notExistingProducts.add(productNumber);
        }
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public List<String> getNotExistingProducts() {
        return notExistingProducts;
    }

    public void setNotExistingProducts(List<String> notExistingProducts) {
        this.notExistingProducts = notExistingProducts;
    }
}
