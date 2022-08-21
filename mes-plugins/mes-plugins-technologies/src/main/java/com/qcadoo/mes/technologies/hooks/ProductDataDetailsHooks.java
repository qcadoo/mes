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
package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.ProductDataFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProductDataDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setNumber(view);
        setProductDataIdForMultiUploadField(view);
        setCriteriaModifierParameters(view);
    }

    private void setNumber(final ViewDefinitionState view) {
        numberGeneratorService
                .generateAndInsertNumber(view, TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_DATA,
                        QcadooViewConstants.L_FORM, ProductDataFields.NUMBER);
    }

    public void setProductDataIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent productDataForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent productDataIdForMultiUpload = (FieldComponent) view.getComponentByReference(TechnologiesConstants.UPLOAD_INPUT_ID);
        FieldComponent productDataMultiUploadLocale = (FieldComponent) view.getComponentByReference(TechnologiesConstants.UPLOAD_INPUT_LOCALE);

        Long productDataId = productDataForm.getEntityId();

        if (Objects.nonNull(productDataId)) {
            productDataIdForMultiUpload.setFieldValue(productDataId);
        } else {
            productDataIdForMultiUpload.setFieldValue("");
        }

        productDataIdForMultiUpload.requestComponentUpdateState();
        productDataMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        productDataMultiUploadLocale.requestComponentUpdateState();
    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(ProductDataFields.TECHNOLOGY);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductDataFields.PRODUCT);

        Entity product = productLookup.getEntity();

        FilterValueHolder filterValueHolder = technologyLookup.getFilterValue();

        if (Objects.isNull(product)) {
            filterValueHolder.remove(ProductDataFields.PRODUCT);
        } else {
            filterValueHolder.put(ProductDataFields.PRODUCT, product.getId());
        }

        technologyLookup.setFilterValue(filterValueHolder);
    }

}
