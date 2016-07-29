package com.qcadoo.mes.deliveries.helpers;

import com.google.common.collect.Lists;

import java.util.List;

public class DeliveredMultiProductContainer {

    private List<DeliveredMultiProduct> deliveredMultiProducts;

    public DeliveredMultiProductContainer() {
        deliveredMultiProducts = Lists.newArrayList();
    }

    public boolean checkIfExsists(DeliveredMultiProduct deliveredMultiProduct) {
        if (deliveredMultiProducts.contains(deliveredMultiProduct)) {
            return true;
        }
        return false;
    }

    public DeliveredMultiProductContainer(List<DeliveredMultiProduct> deliveredMultiProducts) {
        this.deliveredMultiProducts = deliveredMultiProducts;
    }

    public List<DeliveredMultiProduct> getDeliveredMultiProducts() {
        return deliveredMultiProducts;
    }

    public void setDeliveredMultiProducts(List<DeliveredMultiProduct> deliveredMultiProducts) {
        this.deliveredMultiProducts = deliveredMultiProducts;
    }

    public void addProduct(DeliveredMultiProduct deliveredMultiProduct) {
        deliveredMultiProducts.add(deliveredMultiProduct);
    }
}
