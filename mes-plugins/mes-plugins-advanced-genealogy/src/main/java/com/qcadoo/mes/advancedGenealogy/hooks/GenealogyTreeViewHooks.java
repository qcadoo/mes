/**
 * ***************************************************************************
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
 * ***************************************************************************
 */
package com.qcadoo.mes.advancedGenealogy.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenealogyTreeViewHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    // how to fill form with tree
    public final void fillTreeWithExampleData(final ViewDefinitionState view) {
        FormComponent form = getForm(view);
        Entity formEntity = form.getEntity();
        formEntity.setField("genealogyTree", getExampleEntitiesTree());
        form.setEntity(formEntity);
        view.getComponentByReference("genealogyTree").setEnabled(true);
    }

    // dummy genealogy tree generation method
    private EntityTree getExampleEntitiesTree() {
        List<Entity> entities = Lists.newArrayList();

        DataDefinition batchDD = getDataDef(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH);
        List<Entity> batches = batchDD.find().list().getEntities();
        if (batches != null) {
            Entity previousBatch = null;
            for (Entity batch : batches) {
                batch.setField("parent", previousBatch);
                batch.setField("priority", 1);
                batch.setField("entityType", "batch");
                entities.add(batch);
                previousBatch = batch;
            }
        }

        return EntityTreeUtilsService.getDetachedEntityTree(entities);
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
    }

    private DataDefinition getDataDef(final String pluginName, final String modelName) {
        return dataDefinitionService.get(pluginName, modelName);
    }
}
