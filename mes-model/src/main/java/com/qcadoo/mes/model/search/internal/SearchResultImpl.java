/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.model.search.internal;

import java.util.List;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchResult;

public final class SearchResultImpl implements SearchResult {

    private SearchCriteria searchCriteria;

    private List<Entity> results;

    private int totalNumberOfEntities;

    @Override
    public List<Entity> getEntities() {
        return results;
    }

    public void setResults(final List<Entity> results) {
        this.results = results;
    }

    @Override
    public SearchCriteria getCriteria() {
        return searchCriteria;
    }

    public void setCriteria(final SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    public int getTotalNumberOfEntities() {
        return totalNumberOfEntities;
    }

    public void setTotalNumberOfEntities(final int totalNumberOfEntities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
    }

}
