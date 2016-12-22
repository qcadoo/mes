/*
 * **************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
 * **************************************************************************
 */
package com.qcadoo.mes.basic.product.importing;

import com.qcadoo.model.api.Entity;
import org.apache.poi.ss.usermodel.Cell;

import java.util.stream.Stream;

class RowBuilder {

    private static CellBinder[] parsers;

    static {
        parsers = Stream.of("number", "name", "globalTypeOfMaterial", "unit",
                "ean", "category", "description", "producer_id", "assortment_id",
                "parent_id", "nominalCost", "lastOfferCost", "averageOfferCost")
                .map(s -> new CellBinder(s, false))
                .toArray(CellBinder[]::new);
    }

    private Entity entity;
    private boolean finished;
    private int index;
    private boolean empty;

    public boolean isEmpty() {
        return empty;
    }

    void initialize(final Entity entity) {
        this.entity = entity;
        this.finished = false;
        this.index = 0;
        this.empty = true;
    }

    public void append(final Cell cell) {
        if (finished) {
            throw new IllegalStateException("Already finished");
        }
        if (null != cell) {
            empty = false;
        }
        parsers[index++].bind(cell, entity);
    }

    public Entity build() {
        if (finished) {
            throw new IllegalStateException("Already finished");
        }
        finished = true;
        return entity;
    }
}
