package com.qcadoo.mes.cmmsMachineParts.controller.dataProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class ActionsForPlannedEventDataProvider {

    public List<Map<String, String>> getActionStates() {
        return Lists.newArrayList("01correct", "02incorrect").stream().map(unit -> {
            Map<String, String> type = new HashMap<>();
            type.put("value", unit);
            type.put("key", unit);

            return type;
        }).collect(Collectors.toList());
    }
}
