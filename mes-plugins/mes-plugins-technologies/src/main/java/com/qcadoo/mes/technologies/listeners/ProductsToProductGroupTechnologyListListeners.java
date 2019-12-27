package com.qcadoo.mes.technologies.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductsToProductGroupTechnologyListListeners {

    public void openProductsToProductGroupTechnologyImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        view.openModal("../page/technologies/productsToProductGroupTechnologyImport.html");
    }
}
