package com.qcadoo.mes.materialFlow;

import java.math.BigDecimal;
import java.util.Date;

import com.qcadoo.model.api.Entity;

public interface MaterialFlowTransferService {

    public void createTransfer(String type, final Entity stockAreaFrom, final Entity stockAreaTo, final Entity product,
            final BigDecimal quantity, final Entity staff, final Date time);
}
