/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.workPlans.criteriaModifiers;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologyAttachmentFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class WorkPlansCriteriaModifiers {

    public static final String TECHNOLOGY_IDS = "technologyIDs";

    public void showAtachmentsForTechnologies(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        List<Long> technologyIDs = Lists.newArrayList();
        if (!filterValue.has(TECHNOLOGY_IDS)) {
            scb.createAlias(TechnologyAttachmentFields.TECHNOLOGY, "t").add(SearchRestrictions.eq("t.id", 0l));
        } else {
            technologyIDs = filterValue.getListOfLongs(TECHNOLOGY_IDS);
            technologyIDs.isEmpty();
            scb.createAlias(TechnologyAttachmentFields.TECHNOLOGY, "t").add(SearchRestrictions.in("t.id", technologyIDs));
        }
    }
}
