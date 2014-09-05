/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.deviationCausesReporting.domain;

import java.text.Collator;
import java.util.Comparator;

import org.springframework.context.i18n.LocaleContextHolder;

public final class DeviationCauseHolderComparators {

    private DeviationCauseHolderComparators() {
    }

    public static final Comparator<DeviationCauseHolder> BY_REASON_ASC = new Comparator<DeviationCauseHolder>() {

        @Override
        public int compare(final DeviationCauseHolder o1, final DeviationCauseHolder o2) {
            Collator collator = Collator.getInstance(LocaleContextHolder.getLocale());
            return collator.compare(o1.getDeviationCause(), o2.getDeviationCause());
        }
    };

}
