/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.utils;

/**
 * Represents pair key->value
 * 
 * @param <A>
 *            class of key
 * @param <B>
 *            class of value
 */
public final class Pair<A, B> {

    private A key;

    private B value;

    public Pair(final A key, final B value) {
        this.key = key;
        this.value = value;
    }

    public A getKey() {
        return key;
    }

    public B getValue() {
        return value;
    }

    /**
     * creates new Pair instance
     * 
     * @param key
     * @param value
     * @return new Pair containing key and value same as arguments
     */
    public static <A, B> Pair<A, B> of(final A key, final B value) {
        return new Pair<A, B>(key, value);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair other = (Pair) obj;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 37 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("Pair[%s,%s]", key, value);
    }

    public void setKey(A key) {
        this.key = key;
    }

    public void setValue(B value) {
        this.value = value;
    }

}
