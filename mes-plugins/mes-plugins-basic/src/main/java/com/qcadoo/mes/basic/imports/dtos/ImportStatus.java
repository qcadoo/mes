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
package com.qcadoo.mes.basic.imports.dtos;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ImportStatus {

    private int rowsProcessed;

    private Set<ImportError> errors = Sets.newHashSet();

    public int getRowsProcessed() {
        return rowsProcessed;
    }

    public Set<ImportError> getErrors() {
        return ImmutableSet.copyOf(errors);
    }

    public void incrementRowsProcessedCounter() {
        rowsProcessed++;
    }

    public void addError(final ImportError importError) {
        errors.add(importError);
    }

    public boolean hasErrors() {
        return !CollectionUtils.isEmpty(errors);
    }

    public int getErrorsSize() {
        return getErrors().stream().filter(distinctByKey(ImportError::getRowIndex)).collect(Collectors.toList())
                .size();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();

        return (t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImportStatus that = (ImportStatus) o;

        return new EqualsBuilder().append(rowsProcessed, that.rowsProcessed).append(errors, that.errors).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(rowsProcessed).append(errors).toHashCode();
    }

}
