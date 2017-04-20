package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import com.google.common.collect.Maps;

import java.util.Map;

class DeliveryByPalletTypeValue {

   private Map<String, Integer> palletQuantity = Maps.newHashMap();

    public Map<String, Integer> addQuantityForPallet(String palletType, int quantity){
        if(palletQuantity.containsKey(palletType)){
            palletQuantity.put(palletType, palletQuantity.get(palletType) + quantity);
        } else {
            palletQuantity.put(palletType, quantity);
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
