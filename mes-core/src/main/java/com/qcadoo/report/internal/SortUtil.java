/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.report.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class SortUtil {

    private SortUtil() {
        // empty
    }

    public static <T, V> Map<T, V> sortMapUsingComparator(final Map<T, V> operationMap, final Comparator<T> comparator) {
        List<T> operationList = new LinkedList<T>(operationMap.keySet());

        Collections.sort(operationList, comparator);

        Map<T, V> result = new LinkedHashMap<T, V>();

        for (T key : operationList) {
            result.put(key, operationMap.get(key));
        }
        return result;
    }

}
