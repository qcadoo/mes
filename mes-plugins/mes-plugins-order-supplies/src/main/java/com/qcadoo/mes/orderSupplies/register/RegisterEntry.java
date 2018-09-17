package com.qcadoo.mes.orderSupplies.register;

import com.google.common.base.Objects;
import com.qcadoo.model.api.Entity;

public class RegisterEntry {


    private Entity entry;

    private Long productId;

    private Long tocId;

    public RegisterEntry(Entity entry) {
        this.entry = entry;
        this.productId = entry.getBelongsToField("product").getId();
        this.tocId = entry.getBelongsToField("technologyOperationComponent").getId();

    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RegisterEntry that = (RegisterEntry) o;
        return Objects.equal(productId, that.productId) && Objects.equal(tocId, that.tocId);
    }

    @Override public int hashCode() {
        return Objects.hashCode(productId, tocId);
    }
}
