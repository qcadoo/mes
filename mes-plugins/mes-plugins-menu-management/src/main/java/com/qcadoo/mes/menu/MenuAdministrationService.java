package com.qcadoo.mes.menu;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.Restriction;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SimpleCustomRestriction;
import com.qcadoo.report.api.Pair;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.MenuService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.components.form.FormComponentState;
import com.qcadoo.view.components.grid.GridComponentState;
import com.qcadoo.view.components.window.WindowComponentState;

@Service
public class MenuAdministrationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MenuService menuService;

    private static final List<Pair<String, String>> disabledCategories;

    static {
        disabledCategories = new LinkedList<Pair<String, String>>();
        disabledCategories.add(Pair.of("basic", "home"));
        disabledCategories.add(Pair.of("basic", "administration"));
    }

    public void addRestrictionToCategoriesGrid(final ViewDefinitionState viewDefinitionState) {

        GridComponentState categoriesGrid = (GridComponentState) viewDefinitionState.getComponentByReference("grid");

        Restriction[] categoryRestrictions = new Restriction[disabledCategories.size()];
        int index = 0;
        for (Pair<String, String> category : disabledCategories) {
            Restriction pluginEquals = Restrictions.eq("pluginIdentifier", category.getKey());
            Restriction nameEquals = Restrictions.eq("name", category.getValue());
            Restriction sameCategory = Restrictions.and(pluginEquals, nameEquals);
            categoryRestrictions[index++] = Restrictions.not(sameCategory);
        }

        Restriction restriction = Restrictions.and(categoryRestrictions);

        categoriesGrid.setCustomRestriction(new SimpleCustomRestriction(restriction));
    }

    public void translateCategoriesGrid(final ViewDefinitionState viewDefinitionState) {
        GridComponentState categoriesGrid = (GridComponentState) viewDefinitionState.getComponentByReference("grid");
        for (Entity categoryEntity : categoriesGrid.getEntities()) {
            if (categoryEntity.getStringField("pluginIdentifier") != null) {
                categoryEntity.setField("name",
                        menuService.getCategoryTranslation(categoryEntity, viewDefinitionState.getLocale()));
            }
        }
    }

    public void translateCategoryForm(final ViewDefinitionState viewDefinitionState) {
        FormComponentState categoryForm = (FormComponentState) viewDefinitionState.getComponentByReference("form");
        Entity categoryEntity = null;
        if (categoryForm.getEntity() != null) {
            categoryEntity = dataDefinitionService.get("qcadooView", "category").get(categoryForm.getEntity().getId());
        }

        if (categoryEntity != null && categoryEntity.getStringField("pluginIdentifier") != null) {
            ComponentState categoryNameField = viewDefinitionState.getComponentByReference("categoryName");
            categoryNameField.setEnabled(false);
            categoryNameField.setFieldValue(menuService.getCategoryTranslation(categoryEntity, viewDefinitionState.getLocale()));

            disableWinfowButtons(viewDefinitionState);
        }

        GridComponentState categoryItemsGrid = (GridComponentState) viewDefinitionState.getComponentByReference("itemsGrid");
        for (Entity itemEntity : categoryItemsGrid.getEntities()) {
            if (itemEntity.getStringField("pluginIdentifier") != null) {
                itemEntity.setField("name", menuService.getItemTranslation(itemEntity, viewDefinitionState.getLocale()));
            }
        }
    }

    public void translateItemForm(final ViewDefinitionState viewDefinitionState) {
        FormComponentState itemForm = (FormComponentState) viewDefinitionState.getComponentByReference("form");
        Entity itemEntity = null;
        if (itemForm.getEntity() != null) {
            itemEntity = dataDefinitionService.get("qcadooView", "item").get(itemForm.getEntity().getId());
        }
        if (itemEntity != null && itemEntity.getStringField("pluginIdentifier") != null) {
            ComponentState itemNameField = viewDefinitionState.getComponentByReference("itemName");
            itemNameField.setEnabled(false);
            itemNameField.setFieldValue(menuService.getItemTranslation(itemEntity, viewDefinitionState.getLocale()));

            viewDefinitionState.getComponentByReference("itemView").setEnabled(false);

            viewDefinitionState.getComponentByReference("itemActive").setEnabled(false);

            disableWinfowButtons(viewDefinitionState);
        }
    }

    private void disableWinfowButtons(final ViewDefinitionState viewDefinitionState) {
        WindowComponentState window = (WindowComponentState) viewDefinitionState.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        ribbon.getGroupByName("actions").getItemByName("save").setEnabled(false);
        ribbon.getGroupByName("actions").getItemByName("save").setShouldBeUpdated(true);
        ribbon.getGroupByName("actions").getItemByName("saveBack").setEnabled(false);
        ribbon.getGroupByName("actions").getItemByName("saveBack").setShouldBeUpdated(true);
        ribbon.getGroupByName("actions").getItemByName("cancel").setEnabled(false);
        ribbon.getGroupByName("actions").getItemByName("cancel").setShouldBeUpdated(true);
        window.requestRibbonRender();
    }
}
