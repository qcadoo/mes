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

package com.qcadoo.mes.genealogies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.SearchResult;

@Service
@Transactional
public class GenealogyAttributeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Long getGenealogyAttributeId() {

        SearchResult searchResult = dataDefinitionService.get("genealogies", "currentAttribute").find().withMaxResults(1).list();

        if (searchResult.getEntities().size() > 0) {
            return searchResult.getEntities().get(0).getId();
        } else {

            Entity newAttribute = new DefaultEntity("genealogies", "currentAttribute");

            newAttribute.setField("shift", "");
            newAttribute.setField("post", "");
            newAttribute.setField("other", "");

            DataDefinition dataDefinition = dataDefinitionService.get("genealogies", "currentAttribute");
            Entity savedAttribute = dataDefinition.save(newAttribute);

            return savedAttribute.getId();

        }

    }

}
