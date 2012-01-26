/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.internal.states;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class RecordStateListener {

    public List<ChangeRecordStateMessage> onAccepted(final Entity productionRecord, final Entity prevState) {
        return new ArrayList<ChangeRecordStateMessage>();
    }

    public List<ChangeRecordStateMessage> onDeclined(final Entity productionRecord, final Entity prevState) {
        return new ArrayList<ChangeRecordStateMessage>();
    }
}
