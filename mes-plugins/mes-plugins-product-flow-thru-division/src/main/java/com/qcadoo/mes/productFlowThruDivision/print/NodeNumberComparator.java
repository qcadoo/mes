package com.qcadoo.mes.productFlowThruDivision.print;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class NodeNumberComparator implements Comparator<String> {

    @Override
    public int compare(String nodeNumber1, String nodeNumber2) {
        if (nodeNumber1.length() > nodeNumber2.length()) {
            return 1;
        } else if (nodeNumber1.length() == nodeNumber2.length()) {
            List<String> nodeNumber1Parts = Arrays.stream(nodeNumber1.split("\\.")).filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());
            List<String> nodeNumber2Parts = Arrays.stream(nodeNumber2.split("\\.")).filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());
            for (int i = 0, nodeNumber1PartsSize = nodeNumber1Parts.size(); i < nodeNumber1PartsSize; i++) {
                String nodeNumber1Part = nodeNumber1Parts.get(i);
                String nodeNumber2Part = nodeNumber2Parts.get(i);
                if (!nodeNumber1Part.equals(nodeNumber2Part)) {
                    if (StringUtils.isNumeric(nodeNumber1Part)) {
                        return Integer.compare(Integer.parseInt(nodeNumber1Part), Integer.parseInt(nodeNumber2Part));
                    } else {
                        return nodeNumber1Part.compareTo(nodeNumber2Part);
                    }
                }
            }
            return 0;
        } else {
            return -1;
        }
    }
}
