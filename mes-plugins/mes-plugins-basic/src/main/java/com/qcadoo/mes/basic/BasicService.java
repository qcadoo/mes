/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.qcadoo.mes.basic.constants.AddressFields;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.model.constants.QcadooModelConstants;

@Service
public class BasicService {

    private static final String L_MAIN = "01main";

    private static final String L_ADDRESS_TYPE = "addressType";

    private static final String L_GET_NUMBERS_QUERY_TEMPLATE = "SELECT "
            + "SUBSTRING(${NUMBER_FIELD}, '${PREFIX}([0-9]+)') AS ${NUM_PROJECTION_ALIAS}, '' AS nullResultFix "
            + "FROM #basic_address " + "WHERE ${COMPANY_FIELD}.id = ${COMPANY_VALUE} " + "ORDER BY numProjection DESC";

    private static final String L_NUM_PROJECTION_ALIAS = "numProjection";

    private static final String L_DASH = "-";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public String getMainAddressType() {
        String addressType = null;

        Optional<Entity> mayBeMainAddressType = getMainAddressTypeDictionaryItem();

        if (mayBeMainAddressType.isPresent()) {
            Entity mainAddressType = mayBeMainAddressType.get();

            addressType = mainAddressType.getStringField(DictionaryItemFields.NAME);
        }

        return addressType;
    }

    private Optional<Entity> getMainAddressTypeDictionaryItem() {
        Optional<Entity> mayBeAddressType = getAddressTypeDictionary();

        if (mayBeAddressType.isPresent()) {
            Entity addressType = mayBeAddressType.get();

            return Optional.ofNullable(findMainAddressTypeDictionaryItem(addressType));
        }

        return Optional.empty();
    }

    private Entity findMainAddressTypeDictionaryItem(final Entity addressType) {
        return getDictionaryItemDD().find().add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, addressType))
                .add(SearchRestrictions.eq(DictionaryItemFields.TECHNICAL_CODE, L_MAIN)).setMaxResults(1).uniqueResult();
    }

    private Optional<Entity> getAddressTypeDictionary() {
        return Optional.ofNullable(findAddressTypeDictionary());
    }

    private Entity findAddressTypeDictionary() {
        return getDictionaryDD().find().add(SearchRestrictions.eq(DictionaryFields.NAME, L_ADDRESS_TYPE)).setMaxResults(1)
                .uniqueResult();
    }

    public Optional<Entity> getMainAddress(final Entity company) {
        if (company.getId() != null) {
            String addressType = getMainAddressType();

            if (StringUtils.isNotEmpty(addressType)) {
                return Optional.ofNullable(findMainAddress(company, addressType));
            }
        }

        return Optional.empty();
    }

    private Entity findMainAddress(final Entity company, final String addressType) {
        return getAddressDD().find().add(SearchRestrictions.belongsTo(AddressFields.COMPANY, company))
                .add(SearchRestrictions.eq(AddressFields.ADDRESS_TYPE, addressType)).setMaxResults(1).uniqueResult();
    }

    public String generateAddressNumber(final Entity company) {
        String addressNumber = "";

        Long greatestNumber = 0L;

        if (company.getId() != null) {
            Collection<Entity> numberProjections = getNumbersProjection(company);

            Collection<Long> numericValues = extractNumericValues(numberProjections);

            if (!numericValues.isEmpty()) {
                greatestNumber = Ordering.natural().max(numericValues);
            }
        }

        addressNumber = generateNumber(company, greatestNumber + 1);

        return addressNumber;
    }

    private Collection<Entity> getNumbersProjection(final Entity company) {
        List<Entity> numbersProjection = Lists.newArrayList();

        if (company != null) {
            String hqlQuery = buildQuery(company);

            numbersProjection = getAddressDD().find(hqlQuery).list().getEntities();
        }

        return numbersProjection;
    }

    private String buildQuery(final Entity company) {
        String query = "";

        if (company != null) {
            Map<String, String> placeholderValues = Maps.newHashMap();

            placeholderValues.put("NUMBER_FIELD", AddressFields.NUMBER);
            placeholderValues.put("PREFIX", createPrefix(company));
            placeholderValues.put("NUM_PROJECTION_ALIAS", L_NUM_PROJECTION_ALIAS);
            placeholderValues.put("COMPANY_FIELD", AddressFields.COMPANY);
            placeholderValues.put("COMPANY_VALUE", company.getId().toString());

            StrSubstitutor substitutor = new StrSubstitutor(placeholderValues, "${", "}");

            query = substitutor.replace(L_GET_NUMBERS_QUERY_TEMPLATE).toString();
        }

        return query;
    }

    private String createPrefix(final Entity company) {
        String number = company.getStringField(CompanyFields.NUMBER);

        return escapeSql(number) + L_DASH;
    }

    private String escapeSql(final String number) {
        return number.replace("'", "''").replace("(", "\\(").replace(")", "\\)");
    }

    private Collection<Long> extractNumericValues(final Iterable<Entity> numberProjections) {
        List<Long> numericValues = Lists.newArrayList();

        for (Entity projection : numberProjections) {
            String numberFieldValue = projection.getStringField(L_NUM_PROJECTION_ALIAS);

            if (StringUtils.isNumeric(numberFieldValue)) {
                numericValues.add(Long.valueOf(numberFieldValue));
            }
        }

        return numericValues;
    }

    private String generateNumber(final Entity company, final Long number) {
        StringBuilder numberBuilder = new StringBuilder();

        numberBuilder.append(company.getStringField(CompanyFields.NUMBER));
        numberBuilder.append(L_DASH);
        numberBuilder.append(String.format("%02d", number));

        return numberBuilder.toString();
    }

    public boolean checkIfIsMainAddressType(final String addressType) {
        if (StringUtils.isNotEmpty(addressType)) {
            String mainAddressType = getMainAddressType();

            if (StringUtils.isNotEmpty(mainAddressType)) {
                if (mainAddressType.equals(addressType)) {
                    return true;
                }
            }
        }

        return false;
    }

    public DataDefinition getDictionaryDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY);
    }

    public DataDefinition getDictionaryItemDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM);
    }

    public DataDefinition getAddressDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_ADDRESS);
    }

}
