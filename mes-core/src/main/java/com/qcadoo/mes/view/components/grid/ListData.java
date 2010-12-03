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

package com.qcadoo.mes.view.components.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.Entity;

/**
 * View value of GridComponent.
 * 
 * @see com.qcadoo.mes.view.components.GridComponent
 * @see com.qcadoo.mes.view.ViewValue
 */
public final class ListData {

    private Integer totalNumberOfEntities;

    private Long selectedEntityId;

    private Integer firstResult;

    private Integer maxResults;

    private String orderColumn;

    private boolean orderAsc;

    private final List<Map<String, String>> filters = new ArrayList<Map<String, String>>();

    private List<Entity> entities;

    private final String contextFieldName;

    private final Long contextId;

    private boolean searchEnabled = false;

    public ListData() {
        this.contextFieldName = null;
        this.contextId = null;
    }

    public ListData(final int totalNumberOfEntities, final List<Entity> entities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
        this.entities = entities;
        this.contextFieldName = null;
        this.contextId = null;
    }

    public ListData(final int totalNumberOfEntities, final List<Entity> entities, final String contextFieldName,
            final Long contextId) {
        this.totalNumberOfEntities = totalNumberOfEntities;
        this.entities = entities;
        this.contextFieldName = contextFieldName;
        this.contextId = contextId;
    }

    public void setFirstResult(final Integer firstResult) {
        this.firstResult = firstResult;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public void setMaxResults(final Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setOrderAsc(final boolean orderAsc) {
        this.orderAsc = orderAsc;
    }

    public String getOrderColumn() {
        return orderColumn;
    }

    public boolean isOrderAsc() {
        return orderAsc;
    }

    public void addFilter(final String column, final String value) {
        this.filters.add(ImmutableMap.of("column", column, "value", value));
    }

    public void setOrderColumn(final String orderColumnName) {
        this.orderColumn = orderColumnName;
    }

    public Integer getTotalNumberOfEntities() {
        return totalNumberOfEntities;
    }

    public void setTotalNumberOfEntities(final Integer totalNumberOfEntities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(final List<Entity> entities) {
        this.entities = entities;
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    public void setSelectedEntityId(final Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
    }

    public String getContextFieldName() {
        return contextFieldName;
    }

    public Long getContextId() {
        return contextId;
    }

    public Map<String, Integer> getPaging() {
        Map<String, Integer> paging = new HashMap<String, Integer>();
        if (firstResult != null || maxResults != null) {
            if (firstResult != null) {
                paging.put("first", firstResult);
            }
            if (maxResults != null) {
                paging.put("max", maxResults);
            }
        }
        return paging;
    }

    public Map<String, String> getSort() {
        Map<String, String> sort = new HashMap<String, String>();
        if (orderColumn != null) {
            sort.put("column", orderColumn);
            sort.put("order", orderAsc ? "asc" : "desc");
        }
        return sort;
    }

    public List<Map<String, String>> getFilters() {
        return filters;
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

}
