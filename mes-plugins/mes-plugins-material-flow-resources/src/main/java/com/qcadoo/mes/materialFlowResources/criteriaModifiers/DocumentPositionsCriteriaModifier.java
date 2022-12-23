/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.constants.UserFieldsMF;
import com.qcadoo.mes.materialFlow.constants.UserLocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class DocumentPositionsCriteriaModifier {

    public static final String LOCATION_FROM_ID = "locationFrom_id";

    public static final String LOCATION_TO_ID = "locationTo_id";
    public static final String L_ORDER_ID = "orderId";
    public static final String L_STATE = "state";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void restrictToUserLocations(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {

        scb.add(SearchRestrictions.not(SearchRestrictions.and(SearchRestrictions.isNotNull(L_ORDER_ID),
                SearchRestrictions.eq(L_STATE, DocumentState.DRAFT.getStringValue()))));

        Long currentUserId = securityService.getCurrentUserId();
        if (Objects.nonNull(currentUserId)) {
            EntityList userLocations = userDataDefinition().get(currentUserId).getHasManyField(UserFieldsMF.USER_LOCATIONS);
            if (!userLocations.isEmpty()) {
                Set<Integer> locationIds = userLocations.stream().map(ul -> ul.getBelongsToField(UserLocationFields.LOCATION))
                        .mapToInt(e -> e.getId().intValue()).boxed().collect(Collectors.toSet());
                scb.add(SearchRestrictions.or(SearchRestrictions.in(LOCATION_TO_ID, locationIds),
                        SearchRestrictions.in(LOCATION_FROM_ID, locationIds)));
            }
        }
    }

    private DataDefinition userDataDefinition() {
        return dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER);
    }
}
