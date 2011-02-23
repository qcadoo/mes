package com.qcadoo.mes.qualityControls.print.utils;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.mes.api.Entity;

public class EntityBatchNumberComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = 6299937240797213900L;

    @Override
    public int compare(final Entity o1, final Entity o2) {
        String batchNr1 = o1.getField("batchNr") != null ? o1.getField("batchNr").toString() : "";
        String batchNr2 = o2.getField("batchNr") != null ? o2.getField("batchNr").toString() : "";
        return batchNr1.compareTo(batchNr2);
    }

}
