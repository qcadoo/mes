package com.qcadoo.mes.productionCounting.internal.states;

import java.util.ArrayList;
import java.util.List;

import com.qcadoo.model.api.Entity;

public class RecordStateListener {

    public List<ChangeRecordStateMessage> onAccepted(final Entity productionRecord, final Entity prevState) {
        return new ArrayList<ChangeRecordStateMessage>();
    }

    public List<ChangeRecordStateMessage> onDeclined(final Entity productionRecord, final Entity prevState) {
        return new ArrayList<ChangeRecordStateMessage>();
    }
}
