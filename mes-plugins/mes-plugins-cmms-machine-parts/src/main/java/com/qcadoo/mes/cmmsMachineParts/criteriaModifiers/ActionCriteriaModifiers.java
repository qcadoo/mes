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
    
    public void filterAllWithoutOtherAction(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder) {
        filterActionsForObejct(searchCriteriaBuilder, filterValueHolder);
        
        searchCriteriaBuilder.add(SearchRestrictions.or(SearchRestrictions.eq(ActionFields.IS_DEFAULT, false), SearchRestrictions.isNull(ActionFields.IS_DEFAULT)));
    }
    
    public void filterActionsForObejct(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(PlannedEventFields.SUBASSEMBLY)) {
            addSubassemblyCriteria(searchCriteriaBuilder, filterValueHolder);
        } else if (filterValueHolder.has(PlannedEventFields.WORKSTATION)) {
            addWorkstationCriteria(searchCriteriaBuilder, filterValueHolder);
        }
    }
    
    private void addSubassemblyCriteria(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder) {
        Long subassemblyId = filterValueHolder.getLong(PlannedEventFields.SUBASSEMBLY);
        
        addCriteriaForElementAndWorkstationType(searchCriteriaBuilder, filterValueHolder, subassemblyId,
                ActionFields.SUBASSEMBLIES);
    }
    
    private void addWorkstationCriteria(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder) {
        Long workstationId = filterValueHolder.getLong(PlannedEventFields.WORKSTATION);
        
        addCriteriaForElementAndWorkstationType(searchCriteriaBuilder, filterValueHolder, workstationId,
                ActionFields.WORKSTATIONS);
    }
    
    private void addCriteriaForElementAndWorkstationType(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValueHolder, Long elementId, String alias) {
        SearchCriterion searchCriterion;
        
        String other = translationService.translate("cmmsMachineParts.action.name.other", LocaleContextHolder.getLocale());
        
        if (filterValueHolder.has(WorkstationFields.WORKSTATION_TYPE)) {
            Long workstationTypeId = filterValueHolder.getLong(WorkstationFields.WORKSTATION_TYPE);
            
            searchCriterion = SearchRestrictions.or(
                    SearchRestrictions.eq(ActionFields.WORKSTATION_TYPES + ".id", workstationTypeId),
                    SearchRestrictions.eq(alias + ".id", elementId));
        } else {
            searchCriterion = SearchRestrictions.eq(alias + ".id", elementId);
        }
        
        searchCriteriaBuilder.createAlias(ActionFields.WORKSTATION_TYPES, ActionFields.WORKSTATION_TYPES, JoinType.LEFT)
                .createAlias(alias, alias, JoinType.LEFT)
                .add(SearchRestrictions.or(searchCriterion, SearchRestrictions.eq(ActionFields.NAME, other)));
    }
    
}
