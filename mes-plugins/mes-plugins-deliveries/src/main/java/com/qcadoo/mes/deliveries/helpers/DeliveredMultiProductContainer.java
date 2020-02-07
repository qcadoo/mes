package com.qcadoo.mes.deliveries.helpers;

import java.util.List;

import com.google.common.collect.Lists;

public class DeliveredMultiProductContainer {

    private List<DeliveredMultiProduct> deliveredMultiProducts;

    public DeliveredMultiProductContainer() {
        deliveredMultiProducts = Lists.newArrayList();
    }

    public boolean checkIfExists(final DeliveredMultiProduct deliveredMultiProduct) {
        return deliveredMultiProducts.contains(deliveredMultiProduct);
    }

    public DeliveredMultiProductContainer(final List<DeliveredMultiProduct> deliveredMultiProducts) {
        this.deliveredMultiProducts = deliveredMultiProducts;
    }

    public List<DeliveredMultiProduct> getDeliveredMultiProducts() {
        return deliveredMultiProducts;
    }

    public void setDeliveredMultiProducts(final List<DeliveredMultiProduct> deliveredMultiProducts) {
        this.deliveredMultiProducts = deliveredMultiProducts;
    }

    public void addProduct(final DeliveredMultiProduct deliveredMultiProduct) {
        deliveredMultiProducts.add(deliveredMultiProduct);
    }

}
