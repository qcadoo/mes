/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class GenealogyTechnologyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void checkBatchNrReq(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            throw new IllegalStateException("component is not lookup");
        }

        FieldComponent product = (FieldComponent) state;

        FieldComponent batchReq = (FieldComponent) viewDefinitionState.getComponentByReference("batchRequired");

        if (product.getFieldValue() != null) {
            if (batchRequired((Long) product.getFieldValue())) {
                batchReq.setFieldValue("1");
            } else {
                batchReq.setFieldValue("0");
            }
        }
    }

    public void checkAttributesReq(final ViewDefinitionState viewDefinitionState) {

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        if (form.getEntityId() == null) {
            SearchResult searchResult = dataDefinitionService
                    .get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE).find()
                    .setMaxResults(1).list();
            Entity currentAttribute = null;

            if (searchResult.getEntities().size() > 0) {
                currentAttribute = searchResult.getEntities().get(0);
            }

            if (currentAttribute != null) {

                Boolean shiftReq = (Boolean) currentAttribute.getField("shiftReq");
                if (shiftReq != null && shiftReq) {
                    FieldComponent req = (FieldComponent) viewDefinitionState.getComponentByReference("shiftFeatureRequired");
                    req.setFieldValue("1");
                }

                Boolean postReq = (Boolean) currentAttribute.getField("postReq");
                if (postReq != null && postReq) {
                    FieldComponent req = (FieldComponent) viewDefinitionState.getComponentByReference("postFeatureRequired");
                    req.setFieldValue("1");
                }

                Boolean otherReq = (Boolean) currentAttribute.getField("otherReq");
                if (otherReq != null && otherReq) {
                    FieldComponent req = (FieldComponent) viewDefinitionState.getComponentByReference("otherFeatureRequired");
                    req.setFieldValue("1");
                }
            }
        } else {
            return;
        }
    }

    private boolean batchRequired(final Long selectedProductId) {
        Entity product = getProductById(selectedProductId);
        if (product == null) {
            return false;
        } else {
            return product.getField("genealogyBatchReq") == null ? false : (Boolean) product.getField("genealogyBatchReq");
        }
    }

    private Entity getProductById(final Long productId) {
        DataDefinition instructionDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        return instructionDD.get(productId);
    }
}
