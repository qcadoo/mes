package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import com.google.common.collect.Maps;

import java.util.Map;

class DeliveryByPalletTypeValue {

   private Map<String, Integer> palletQuantity = Maps.newHashMap();

    public Map<String, Integer> addQuantityForPallet(String typeOfLoadUnit, int quantity){
        if(palletQuantity.containsKey(typeOfLoadUnit)){
            palletQuantity.put(typeOfLoadUnit, palletQuantity.get(typeOfLoadUnit) + quantity);
        } else {
            palletQuantity.put(typeOfLoadUnit, quantity);
        }
        return palletQuantity;
    }

    public int sum() {
        return palletQuantity.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<String, Integer> getPalletQuantity() {
        return palletQuantity;
    }

    public void setPalletQuantity(Map<String, Integer> palletQuantity) {
        this.palletQuantity = palletQuantity;
    }
}
