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

package com.qcadoo.mes.products;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.RestrictionOperator;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;

@Service
public final class TechnologyService {

    public boolean clearMasterOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("master", false);
        return true;
    }

    public boolean copyTechnologyFromParent(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField("parent") == null) {
            return true;
        }

        Object object = entity.getField("parent");

        Entity parent = null;

        if (object instanceof Long) {
            parent = dataDefinition.get((Long) object);
        } else {
            return false;
        }

        entity.setField("technology", parent.getField("technology"));

        return true;
    }

    public boolean checkTechnologyDefault(final DataDefinition dataDefinition, final Entity entity) {
        Boolean master = (Boolean) entity.getField("master");

        if (!master) {
            return true;
        }

        SearchCriteriaBuilder searchCriteria = dataDefinition.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(dataDefinition.getField("master"), true))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("product"), entity.getField("product")));

        if (entity.getId() != null) {
            searchCriteria.restrictedWith(Restrictions.idRestriction(entity.getId(), RestrictionOperator.NE));
        }

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 0) {
            return true;
        } else {
            entity.addError(dataDefinition.getField("master"), "products.validate.global.error.default");
            return false;
        }
    }

}
