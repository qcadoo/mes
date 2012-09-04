/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.basic;

import static com.qcadoo.mes.basic.constants.ConversionItemFields.PRODUCT;
import static com.qcadoo.mes.basic.constants.ConversionItemFields.QUANTITY_FROM;
import static com.qcadoo.mes.basic.constants.ConversionItemFields.QUANTITY_TO;
import static com.qcadoo.mes.basic.constants.ConversionItemFields.UNIT_FROM;
import static com.qcadoo.mes.basic.constants.ConversionItemFields.UNIT_TO;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ConversionService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public Long getParameterId() {
        DataDefinition dataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_CONVERSION);
        Entity conversion = dataDefinition.find().setMaxResults(1).uniqueResult();

        if (conversion == null) {
            Entity newConversion = dataDefinition.create();
            Entity savedConversion = dataDefinition.save(newConversion);
            return savedConversion.getId();

        } else {
            return conversion.getId();
        }

    }

    public void getUnitConversionTree(final DataDefinition conversionDD, String unit, ConversionTree parent,
            final List<ConversionTree> conversionTreeList) {
        int y = 0;

        while (y <= conversionTreeList.size()) {

            final List<Entity> left = conversionDD.find().add(SearchRestrictions.eq(UNIT_FROM, unit))
                    .add(SearchRestrictions.neField(UNIT_FROM, UNIT_TO)).add(SearchRestrictions.isNull(PRODUCT)).list()
                    .getEntities();

            final List<Entity> right = conversionDD.find().add(SearchRestrictions.eq(UNIT_TO, unit))
                    .add(SearchRestrictions.neField(UNIT_FROM, UNIT_TO)).add(SearchRestrictions.isNull(PRODUCT)).list()
                    .getEntities();

            if (!left.isEmpty()) {

                for (int i = 0; i < left.size(); i++) {

                    if (checkList(conversionTreeList, left.get(i).getStringField(UNIT_TO))) {

                        addConversionToTree((BigDecimal) left.get(i).getField(QUANTITY_FROM),
                                (BigDecimal) left.get(i).getField(QUANTITY_TO), left.get(i).getStringField(UNIT_FROM), left
                                        .get(i).getStringField(UNIT_TO), left, i, parent, conversionTreeList);

                    }
                }

            }
            if (!right.isEmpty()) {

                for (int i = 0; i < right.size(); i++) {

                    if (checkList(conversionTreeList, right.get(i).getStringField(UNIT_FROM))) {

                        addConversionToTree((BigDecimal) right.get(i).getField(QUANTITY_TO),
                                (BigDecimal) right.get(i).getField(QUANTITY_FROM), right.get(i).getStringField(UNIT_TO), right
                                        .get(i).getStringField(UNIT_FROM), right, i, parent, conversionTreeList);

                    }
                }
            }

            y++;

            if ((y > conversionTreeList.size() - 1)) {

                break;

            } else {

                unit = conversionTreeList.get(y).getUnitTo();
                parent = conversionTreeList.get(y);

            }
        }

    }

    public void addConversionToTree(final BigDecimal quntityFrom, final BigDecimal quntityTo, final String unitFrom,
            final String unitTo, List<Entity> list, int i, ConversionTree parent, List<ConversionTree> conversionTreeList) {
        ConversionTree ct = new ConversionTree();
        ct.setQuantityFrom(quntityFrom);
        ct.setQuantityTo(quntityTo);
        ct.setUnitFrom(unitFrom);
        ct.setUnitTo(unitTo);
        ct.setParent(parent);

        if (!ct.getQuantityFrom().equals(BigDecimal.ONE)) {
            BigDecimal div = ct.getQuantityTo().divide(ct.getQuantityFrom(), numberService.getMathContext());
            ct.setQuantityTo(div);
            ct.setQuantityFrom(BigDecimal.ONE);
        }
        conversionTreeList.add(ct);

    }

    public void calculateTree(final ConversionTree node, final String unit) {
        if (!node.getUnitFrom().equals(unit) && node.getParent().getUnitFrom() != null) {
            node.setQuantityTo(node.getQuantityTo().multiply(node.getParent().getQuantityTo(), numberService.getMathContext()));
            node.setUnitFrom(node.getParent().getUnitFrom());
            calculateTree(node.getParent(), unit);
        }
    }

    public boolean checkList(final List<ConversionTree> conversionTreeList, final String unit) {
        if (!conversionTreeList.isEmpty()) {
            for (int j = 0; j < conversionTreeList.size(); j++) {
                if (unit.equals(conversionTreeList.get(j).getUnitTo())) {
                    return false;
                }
            }

            return true;
        }
        return true;
    }

}
