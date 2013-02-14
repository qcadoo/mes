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
package com.qcadoo.mes.genealogies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;

@Service
@Transactional
public class GenealogyAttributeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Long getGenealogyAttributeId() {
        DataDefinition dataDefinition = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE);

        SearchResult searchResult = dataDefinition.find().setMaxResults(1).list();

        if (searchResult.getEntities().size() > 0) {
            return searchResult.getEntities().get(0).getId();
        } else {

            Entity newAttribute = dataDefinition.create();

            newAttribute.setField("shift", "");
            newAttribute.setField("post", "");
            newAttribute.setField("other", "");

            Entity savedAttribute = dataDefinition.save(newAttribute);

            return savedAttribute.getId();

        }

    }

}
