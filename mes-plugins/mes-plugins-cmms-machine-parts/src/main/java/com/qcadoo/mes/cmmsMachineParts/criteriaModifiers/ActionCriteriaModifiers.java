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
package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ActionCriteriaModifiers {

    @Autowired
    private TranslationService translationService;

    public void filterActionsForObejct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(PlannedEventFields.SUBASSEMBLY)) {
            addSubassemblyCriteria(scb, filterValue);

        } else if (filterValue.has(PlannedEventFields.WORKSTATION)) {
            addWorkstationCriteria(scb, filterValue);
        }

    }

    private void addSubassemblyCriteria(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        Long subassemblyId = filterValue.getLong(PlannedEventFields.SUBASSEMBLY);
        addCriteriaForElementAndWorkstationType(scb, filterValue, subassemblyId, ActionFields.SUBASSEMBLIES);
    }

    private void addWorkstationCriteria(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        Long workstationId = filterValue.getLong(PlannedEventFields.WORKSTATION);
        addCriteriaForElementAndWorkstationType(scb, filterValue, workstationId, ActionFields.WORKSTATIONS);
    }

    private void addCriteriaForElementAndWorkstationType(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue,
            Long elementId, String alias) {
        SearchCriterion criterion;
        String other = translationService.translate("cmmsMachineParts.action.name.other", LocaleContextHolder.getLocale());
        if (filterValue.has(WorkstationFields.WORKSTATION_TYPE)) {
            Long workstationTypeId = filterValue.getLong(WorkstationFields.WORKSTATION_TYPE);
            criterion = SearchRestrictions.or(SearchRestrictions.eq(ActionFields.WORKSTATION_TYPES + ".id", workstationTypeId),
                    SearchRestrictions.eq(alias + ".id", elementId));
        } else {
            criterion = SearchRestrictions.eq(alias + ".id", elementId);
        }
        scb.createAlias(ActionFields.WORKSTATION_TYPES, ActionFields.WORKSTATION_TYPES, JoinType.LEFT)
                .createAlias(alias, alias, JoinType.LEFT)
                .add(SearchRestrictions.or(criterion, SearchRestrictions.eq(ActionFields.NAME, other)));
    }
}
