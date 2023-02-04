package com.qcadoo.mes.basic.hooks;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PieceRateFields;
import com.qcadoo.mes.basic.constants.PieceRateItemFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class PieceRateHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        setNumber(entity);
    }

    public void onView(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(PieceRateFields.CURRENT_RATE, findCurrentRate(entity));
    }

    private void setNumber(final Entity entity) {
        if (checkIfShouldInsertNumber(entity)) {
            String number = jdbcTemplate.queryForObject("select generate_piece_rate_number()", Collections.emptyMap(),
                    String.class);
            entity.setField(PieceRateFields.NUMBER, number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity entity) {
        if (!Objects.isNull(entity.getId())) {
            return false;
        }
        return !StringUtils.isNotBlank(entity.getStringField(PieceRateFields.NUMBER));
    }


    private BigDecimal findCurrentRate(Entity pieceRate) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PIECE_RATE_ITEM)
                .find().addOrder(SearchOrders.desc(PieceRateItemFields.DATE_FROM))
                .add(SearchRestrictions.belongsTo(PieceRateItemFields.PIECE_RATE, pieceRate))
                .add(SearchRestrictions.le(PieceRateItemFields.DATE_FROM, new Date()));
        Entity pieceRateItem = scb.setMaxResults(1).uniqueResult();
        if (Objects.isNull(pieceRateItem)) {
            return null;
        } else {
            return pieceRateItem.getDecimalField(PieceRateItemFields.ACTUAL_RATE);
        }

    }

}
