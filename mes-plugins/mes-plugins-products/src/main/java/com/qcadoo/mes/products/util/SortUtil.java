package com.qcadoo.mes.products.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SortUtil {

    public static <T, V> Map<T, V> sortMapUsingComparator(Map<T, V> operationMap, Comparator<T> comparator) {
        List<T> operationList = new LinkedList<T>(operationMap.keySet());

        Collections.sort(operationList, comparator);

        Map<T, V> result = new LinkedHashMap<T, V>();

        for (T key : operationList) {
            result.put(key, operationMap.get(key));
        }
        return result;
    }

}
