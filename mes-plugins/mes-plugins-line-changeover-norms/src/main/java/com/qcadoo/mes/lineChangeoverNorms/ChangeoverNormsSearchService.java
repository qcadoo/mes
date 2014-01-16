/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.lineChangeoverNorms;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public interface ChangeoverNormsSearchService {

    Entity findBestMatching(final Long fromTechnologyId, final Long fromTechnologyGroupId, final Long toTechnologyId,
            final Long toTechnologyGroupId, final Long productionLineId);

    @Deprecated
    /**
     * @deprecated use ChangeoverNormsSearchService#findBestMatching
     */
    Entity searchMatchingChangeroverNormsForTechnologyWithLine(final Entity fromTechnology, final Entity toTechnology,
            final Entity productionLine);

    @Deprecated
    /**
     * @deprecated use ChangeoverNormsSearchService#findBestMatching
     */
    Entity searchMatchingChangeroverNormsForTechnologyGroupWithLine(final Entity fromTechnologyGroup,
            final Entity toTechnologyGroup, final Entity productionLine);

}
