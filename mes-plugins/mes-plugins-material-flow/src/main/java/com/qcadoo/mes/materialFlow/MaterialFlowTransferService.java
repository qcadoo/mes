package com.qcadoo.mes.materialFlow;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.qcadoo.model.api.Entity;

public interface MaterialFlowTransferService {

    public void createTransfer(final String type, final Entity stockAreaFrom, final Entity stockAreaTo, final Entity product,
            final BigDecimal quantity, final Entity staff, final Date time);

    public List<Entity> getTransferTemplates(final Entity stockAreaFrom, final Entity stockAreaTo);
}
