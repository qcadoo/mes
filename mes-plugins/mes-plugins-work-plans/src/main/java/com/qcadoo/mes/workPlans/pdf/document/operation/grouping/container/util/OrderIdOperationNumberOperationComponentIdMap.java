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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.util;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class OrderIdOperationNumberOperationComponentIdMap {

    private Map<Long, Multimap<String, Long>> map;

    private OrderIdOperationNumberOperationComponentIdMap() {
        this.map = Maps.newHashMap();
    }

    public static OrderIdOperationNumberOperationComponentIdMap create() {
        return new OrderIdOperationNumberOperationComponentIdMap();
    }

    public void put(Long orderId, String operationNumber, Long operationComponentId) {
        Multimap<String, Long> innerMap = map.get(orderId);
        if(innerMap == null) {
            innerMap = HashMultimap.create();
            map.put(orderId, innerMap);
        }
        innerMap.put(operationNumber, operationComponentId);
    }

    public Collection<Long> get(Long orderId, String operationNumber) {
        Multimap<String, Long> innerMap = map.get(orderId);
        return isEmpty(innerMap) ? null : innerMap.get(operationNumber);
    }

    public boolean containsKey(Long orderId, String operationNumber) {
        Multimap<String, Long> innerMap = map.get(orderId);
        return !isEmpty(innerMap) && innerMap.containsKey(operationNumber);
    }

    private boolean isEmpty(Multimap<String, Long> innerMap) {
        return innerMap == null || innerMap.isEmpty();
    }

}
