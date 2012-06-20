package com.qcadoo.mes.materialFlow;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.qcadoo.model.api.Entity;

public interface MaterialFlowTransferService {

    void createTransfer(final String type, final Entity stockAreasFrom, final Entity stockAreasTo, final Entity product,
            final BigDecimal quantity, final Entity staff, final Date time);

    List<Entity> getTransferTemplates(final Entity stockAreasFrom, final Entity stockAreasTo);

    boolean isTransferValidAndAreResourcesSufficient(final Entity stockAreasFrom, final Entity product, final BigDecimal quantity);
}
