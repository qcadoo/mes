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

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.products.ProductsProduct;
import com.qcadoo.mes.beans.products.ProductsSubstitute;
import com.qcadoo.mes.beans.products.ProductsTechnology;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.FieldComponentPattern;
import com.qcadoo.mes.view.components.PasswordComponentPattern;
import com.qcadoo.mes.view.components.TextInputComponentPattern;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.components.layout.GridLayoutCell;
import com.qcadoo.mes.view.components.layout.GridLayoutPattern;

@Service
public final class ProductService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfSubstituteIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        ProductsSubstitute substitute = (ProductsSubstitute) entity.getField("substitute");

        if (substitute == null || substitute.getId() == null) {
            return true;
        }

        Entity substituteEntity = dataDefinitionService.get("products", "substitute").get(substitute.getId());

        if (substituteEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("substitute", null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkSubstituteComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsProduct product = (ProductsProduct) entity.getField("product");
        ProductsSubstitute substitute = (ProductsSubstitute) entity.getField("substitute");

        if (substitute == null || product == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find()
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("product"), product.getId()))
                .restrictedWith(Restrictions.belongsTo(dataDefinition.getField("substitute"), substitute.getId())).list();

        if (searchResult.getTotalNumberOfEntities() > 0 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("product"), "products.validate.global.error.substituteComponentDuplicated");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfProductIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        ProductsProduct product = (ProductsProduct) entity.getField("product");

        if (product == null || product.getId() == null) {
            return true;
        }

        Entity productEntity = dataDefinitionService.get("products", "product").get(product.getId());

        if (productEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("product", null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfTechnologyIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        ProductsTechnology technology = (ProductsTechnology) entity.getField("technology");

        if (technology == null || technology.getId() == null) {
            return true;
        }

        Entity technologyEntity = dataDefinitionService.get("products", "technology").get(technology.getId());

        if (technologyEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("technology", null);
            return false;
        } else {
            return true;
        }
    }

    @Autowired
    private TranslationService translationService;

    public void test(final ViewDefinition viewDefinition, final JSONObject jsonObject, final Locale locale) {
        Long id = null;

        try {
            if (jsonObject.has("window.product.id") && !jsonObject.isNull("window.product.id")) {
                id = jsonObject.getLong("window.product.id");
            } else {
                JSONObject jsonObject1 = getJsonObject(jsonObject, "components", "window", "components", "product", "context");

                if (jsonObject1 != null && jsonObject1.has("id") && !jsonObject1.isNull("id")) {
                    id = jsonObject1.getLong("id");
                }
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        FormComponentPattern form = (FormComponentPattern) viewDefinition.getComponentByReference("form");
        GridLayoutPattern gridLayout = (GridLayoutPattern) viewDefinition.getComponentByReference("gridLayout");

        ComponentDefinition componentDefinition = new ComponentDefinition();
        componentDefinition.setName("number");
        componentDefinition.setFieldPath("number");
        componentDefinition.setParent(gridLayout);
        componentDefinition.setTranslationService(translationService);
        componentDefinition.setViewDefinition(viewDefinition);

        FieldComponentPattern number = null;

        if (id == null) {
            number = new PasswordComponentPattern(componentDefinition);
        } else {
            number = new TextInputComponentPattern(componentDefinition);
        }

        number.initialize();

        gridLayout.addChild(number);

        form.addFieldEntityIdChangeListener("number", number);

        GridLayoutCell cell = gridLayout.getCells()[0][0];
        cell.setComponent(number);
    }

    private JSONObject getJsonObject(JSONObject jsonObject, final String... path) throws JSONException {
        for (String p : path) {
            if (!jsonObject.has(p)) {
                return null;
            }

            jsonObject = jsonObject.getJSONObject(p);
        }

        return jsonObject;
    }

}
