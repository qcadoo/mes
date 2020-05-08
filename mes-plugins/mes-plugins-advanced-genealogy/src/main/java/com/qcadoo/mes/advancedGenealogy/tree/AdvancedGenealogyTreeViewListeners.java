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
package com.qcadoo.mes.advancedGenealogy.tree;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdvancedGenealogyTreeViewListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private AdvancedGenealogyTreeService advancedGenealogyTreeService;

    Entity generateFormEntity(final ViewDefinitionState view, final ComponentState state) {
        DataDefinition dd = dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                AdvancedGenealogyConstants.MODEL_BATCH);

        FormComponent form = getForm(view);
        Entity formEntity = form.getEntity();
        Long batchId = (Long) view.getComponentByReference("batchLookup").getFieldValue();

        if (batchId == null) {
            throw new FormValidationException("advancedGenealogy.genealogyTree.noBatchSelected");
        }

        Entity producedBatch = dd.get(batchId);
        String includeDraftString = (String) view.getComponentByReference("includeDrafts").getFieldValue();
        boolean includeDrafts = "1".equals(includeDraftString);

        List<Entity> tree;

        String treeType = (String) view.getComponentByReference("treeType").getFieldValue();

        if ("01producedFrom".equals(treeType)) {
            tree = advancedGenealogyTreeService.getProducedFromTree(producedBatch, includeDrafts, true);
        } else if ("02usedToProduce".equals(treeType)) {
            tree = advancedGenealogyTreeService.getUsedToProduceTree(producedBatch, includeDrafts, true);
        } else {
            throw new FormValidationException("advancedGenealogy.genealogyTree.noTreeTypeSelected");
        }

        EntityTree entityTree = EntityTreeUtilsService.getDetachedEntityTree(tree);
        formEntity.setField("producedBatch", dd.get(batchId));
        formEntity.setField("genealogyTree", entityTree);

        return formEntity;
    }

    public final void generateTree(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        try {
            Entity formEntity = generateFormEntity(view, state);

            FormComponent form = getForm(view);
            form.setEntity(formEntity);
            view.getComponentByReference("genealogyTree").setEnabled(true);
        } catch (FormValidationException e) {
            state.addMessage(e.getMessage(), MessageType.FAILURE);
        }
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
    }

    static class FormValidationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        FormValidationException(final String msg) {
            super(msg);
        }
    }

}
