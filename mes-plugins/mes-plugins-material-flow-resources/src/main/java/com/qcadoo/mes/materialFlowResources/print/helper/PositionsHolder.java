package com.qcadoo.mes.materialFlowResources.print.helper;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.List;

public class PositionsHolder {

    private NumberService numberService;

    private List<Position> positions = Lists.newArrayList();

    public PositionsHolder(NumberService numberService) {
        this.numberService = numberService;
    }

    public PositionsHolder addPosition(Position position) {
        if (positions.contains(position)) {
            BigDecimal value = positions.get(positions.indexOf(position)).getQuantity();
            value = value.add(position.getQuantity(), numberService.getMathContext());
            positions.get(positions.indexOf(position)).setQuantity(value);
        } else {
            positions.add(position);
        }
        return this;
    }

    public List<Position> getPositions() {
        return positions;
    }
}
