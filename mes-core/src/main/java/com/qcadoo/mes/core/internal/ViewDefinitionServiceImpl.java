package com.qcadoo.mes.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.api.ViewDefinitionService;
import com.qcadoo.mes.core.enums.PluginStatus;
import com.qcadoo.mes.core.internal.view.ViewDefinitionImpl;
import com.qcadoo.mes.core.view.ViewDefinition;

@Service
public final class ViewDefinitionServiceImpl implements ViewDefinitionService {

    @Autowired
    private PluginManagementService pluginManagementService;

    private final Map<String, ViewDefinition> viewDefinitions = new HashMap<String, ViewDefinition>();

    @Override
    @Transactional(readOnly = true)
    public ViewDefinition get(final String pluginIdentifier, final String viewName) {
        ViewDefinition viewDefinition = null;
        if (StringUtils.hasText(pluginIdentifier)) {
            viewDefinition = viewDefinitions.get(pluginIdentifier + "." + viewName);
        } else {
            viewDefinition = viewDefinitions.get(viewName); // TODO remove it
        }
        PluginsPlugin plugin = pluginManagementService.getPluginByIdentifierAndStatus(viewDefinition.getPluginIdentifier(),
                PluginStatus.ACTIVE.getValue());
        if (plugin != null) {
            return viewDefinition;
        }
        return new ViewDefinitionImpl(null, "main");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewDefinition> list() {
        List<ViewDefinition> viewsList = new ArrayList<ViewDefinition>();
        List<PluginsPlugin> activePluginList = pluginManagementService.getActivePlugins();
        for (PluginsPlugin activePlugin : activePluginList) {
            for (ViewDefinition viewDefinition : viewDefinitions.values()) {
                if (activePlugin.getIdentifier().equals(viewDefinition.getPluginIdentifier())) {
                    viewsList.add(viewDefinition);
                }
            }
        }

        Collections.sort(viewsList, new Comparator<ViewDefinition>() {

            @Override
            public int compare(final ViewDefinition v1, final ViewDefinition v2) {
                return v1.getName().compareTo(v2.getName());
            }

        });

        return viewsList;
    }

    @Override
    public void save(final ViewDefinition viewDefinition) {
        viewDefinitions.put(viewDefinition.getPluginIdentifier() + "." + viewDefinition.getName(), viewDefinition);
    }

    @Override
    public void delete(final String pluginIdentifier, final String viewName) {
        viewDefinitions.remove(pluginIdentifier + "." + viewName);
    }

    // private ViewDefinitionImpl createTestGridView() {
    // DataDefinition testADD = dataDefinitionService.get("products", "testBeanA");
    //
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", testADD, "products.grid");
    // windowDefinition.addRawOption(new ComponentOption("backButton", ImmutableMap.of("value", "false")));
    //
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.grid");
    // viewDefinition.setRoot(windowDefinition);
    //
    // GridComponent grid = new GridComponent("beansAGrid", windowDefinition, null, null);
    // grid.addRawOption(new ComponentOption("correspondingView", ImmutableMap.of("value", "products.form")));
    // grid.addRawOption(new ComponentOption("paging", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("sortable", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("filter", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("multiselect", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("height", ImmutableMap.of("value", "450")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value", "name")));
    //
    // windowDefinition.addComponent(grid);
    //
    // windowDefinition.initialize();
    //
    // return viewDefinition;
    // }
    //
    // private ViewDefinitionImpl createTestFormView() {
    // DataDefinition testADD = dataDefinitionService.get("products", "testBeanA");
    // DataDefinition testBDD = dataDefinitionService.get("products", "testBeanB");
    // DataDefinition testCDD = dataDefinitionService.get("products", "testBeanC");
    //
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", testADD, "products.form");
    //
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.form");
    // viewDefinition.setRoot(windowDefinition);
    //
    // FormComponent formDefinition = new FormComponent("beanAForm", windowDefinition, null, null);
    // formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
    // formDefinition.addComponent(new CheckBoxComponent("active", formDefinition, "active", null));
    // formDefinition.addComponent(new EntityComboBox("beanB", formDefinition, "beanB", null));
    // formDefinition.addComponent(new EntityComboBox("beanA", formDefinition, "beanA", "#{mainWindow.beanAForm.beanB}.beansA"));
    // formDefinition.addComponent(new TextInputComponent("nameM", formDefinition, null, null));
    // formDefinition.addComponent(new TextInputComponent("nameB", formDefinition, "beanB.name", null));
    // formDefinition.addComponent(new TextInputComponent("nameC", formDefinition, "beanB.beanC.name", null));
    //
    // GridComponent beanAForm_beansCGrig = new GridComponent("beansCGrig", formDefinition, null, "beansC");
    // beanAForm_beansCGrig.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value", "name")));
    //
    // formDefinition.addComponent(beanAForm_beansCGrig);
    // windowDefinition.addComponent(formDefinition);
    //
    // GridComponent beansBGrig = new GridComponent("beansBGrig", windowDefinition, null,
    // "#{mainWindow.beanAForm.beansCGrig}.beansB");
    // beansBGrig.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value", "name")));
    //
    // windowDefinition.addComponent(beansBGrig);
    //
    // FormComponent formCDefinition = new FormComponent("beanCForm", windowDefinition, null, "#{mainWindow.beansBGrig}.beanC");
    // formCDefinition.addComponent(new TextInputComponent("name", formCDefinition, "name", null));
    // FormComponent formCDefinition_formA = new FormComponent("formA", formCDefinition, "beanA", null);
    // formCDefinition_formA.addComponent(new TextInputComponent("name", formCDefinition_formA, "name", null));
    // formCDefinition.addComponent(formCDefinition_formA);
    // GridComponent beanCForm_beansBGrig = new GridComponent("beansBGrig", formCDefinition, null, "beansB");
    // beanCForm_beansBGrig.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value", "name")));
    // formCDefinition.addComponent(beanCForm_beansBGrig);
    // formCDefinition.addComponent(new TextInputComponent("nameB", formCDefinition, null,
    // "#{mainWindow.beanCForm.beansBGrig}.name"));
    // windowDefinition.addComponent(formCDefinition);
    //
    // FormComponent formBDefinition = new FormComponent("beanBForm", windowDefinition, null, "#{mainWindow.beansBGrig}");
    // formBDefinition.addComponent(new TextInputComponent("name", formBDefinition, "name", null));
    // windowDefinition.addComponent(formBDefinition);
    //
    // windowDefinition.initialize();
    //
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createProductGridView() {
    // DataDefinition productDataDefinition = dataDefinitionService.get("products", "product");
    //
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", productDataDefinition, "products.productGridView");
    // windowDefinition.addRawOption(new ComponentOption("backButton", ImmutableMap.of("value", "false")));
    //
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.productGridView");
    // viewDefinition.setRoot(windowDefinition);
    //
    // GridComponent grid = new GridComponent("productsGrid", windowDefinition, null, null);
    // grid.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
    // grid.addRawOption(new ComponentOption("correspondingView", ImmutableMap.of("value", "products.productDetailsView")));
    // grid.addRawOption(new ComponentOption("paging", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("sortable", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("filter", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("multiselect", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("height", ImmutableMap.of("value", "450")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "number", "value", "number")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value", "name")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "typeOfMaterial", "value", "typeOfMaterial")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "ean", "value", "ean")));
    // windowDefinition.addComponent(grid);
    //
    // windowDefinition.initialize();
    //
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createProductDetailsView() {
    // DataDefinition productDataDefinition = dataDefinitionService.get("products", "product");
    // DataDefinition substituteDataDefinition = dataDefinitionService.get("products", "substitute");
    // DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products", "substituteComponent");
    //
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", productDataDefinition, "products.productDetailsView");
    //
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.productDetailsView");
    // viewDefinition.setRoot(windowDefinition);
    //
    // FormComponent formDefinition = new FormComponent("productDetailsForm", windowDefinition, null, null);
    // windowDefinition.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
    //
    // formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
    // formDefinition.addComponent(new TextInputComponent("number", formDefinition, "number", null));
    // formDefinition.addComponent(new DynamicComboBox("typeOfMaterial", formDefinition, "typeOfMaterial", null));
    // formDefinition.addComponent(new TextInputComponent("ean", formDefinition, "ean", null));
    // formDefinition.addComponent(new DynamicComboBox("category", formDefinition, "category", null));
    // formDefinition.addComponent(new TextInputComponent("unit", formDefinition, "unit", null));
    // windowDefinition.addComponent(formDefinition);
    //
    // GridComponent substituteGridDefinition = new GridComponent("substitutesGrid", windowDefinition, "substitutes", null);
    // substituteGridDefinition
    // .addRawOption(new ComponentOption("column", ImmutableMap.of("name", "number", "value", "number")));
    // substituteGridDefinition.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value", "name")));
    // substituteGridDefinition.addRawOption(new ComponentOption("column", ImmutableMap.of("priority", "number", "value",
    // "priority")));
    // substituteGridDefinition.addRawOption(new ComponentOption("paging", ImmutableMap.of("value", "false")));
    // substituteGridDefinition.addRawOption(new ComponentOption("sortable", ImmutableMap.of("value", "false")));
    // substituteGridDefinition.addRawOption(new ComponentOption("filter", ImmutableMap.of("value", "false")));
    // substituteGridDefinition.addRawOption(new ComponentOption("multiselect", ImmutableMap.of("value", "false")));
    // substituteGridDefinition.addRawOption(new ComponentOption("height", ImmutableMap.of("value", "150")));
    // substituteGridDefinition.addRawOption(new ComponentOption("correspondingView", ImmutableMap.of("value",
    // "products.substituteDetailsView")));
    // windowDefinition.addComponent(substituteGridDefinition);
    //
    // GridComponent substituteComponentGridDefinition = new GridComponent("substitutesComponentGrid", windowDefinition, null,
    // "#{mainWindow.substitutesGrid}.components");
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "number", "value",
    // "product", "expression", "#product['number']")));
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value",
    // "product", "expression", "#product['name']")));
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "quantity", "value",
    // "quantity")));
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("paging", ImmutableMap.of("value", "false")));
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("sortable", ImmutableMap.of("value", "false")));
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("filter", ImmutableMap.of("value", "false")));
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("multiselect", ImmutableMap.of("value", "false")));
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("height", ImmutableMap.of("value", "150")));
    // substituteComponentGridDefinition.addRawOption(new ComponentOption("correspondingView", ImmutableMap.of("value",
    // "products.substituteComponentDetailsView")));
    // windowDefinition.addComponent(substituteComponentGridDefinition);
    //
    // windowDefinition.initialize();
    //
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createProductSubstituteDetailsView() {
    // DataDefinition substituteDataDefinition = dataDefinitionService.get("products", "substitute");
    //
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", substituteDataDefinition,
    // "products.substituteDetailsView");
    //
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.substituteDetailsView");
    // viewDefinition.setRoot(windowDefinition);
    //
    // FormComponent formDefinition = new FormComponent("substitutesDetailsForm", windowDefinition, null, null);
    // windowDefinition.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
    // formDefinition.addComponent(new TextInputComponent("number", formDefinition, "number", null));
    // formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
    // formDefinition.addComponent(new TextInputComponent("priority", formDefinition, "priority", null));
    // formDefinition.addComponent(new TextInputComponent("effectiveDateFrom", formDefinition, "effectiveDateFrom", null));
    // formDefinition.addComponent(new TextInputComponent("effectiveDateTo", formDefinition, "effectiveDateTo", null));
    //
    // windowDefinition.addComponent(formDefinition);
    //
    // windowDefinition.initialize();
    //
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createProductSubstituteComponentDetailsView() {
    // DataDefinition substitutesComponentDataDefinition = dataDefinitionService.get("products", "substituteComponent");
    //
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", substitutesComponentDataDefinition,
    // "products.substituteComponentDetailsView");
    //
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.substituteComponentDetailsView");
    // viewDefinition.setRoot(windowDefinition);
    //
    // FormComponent formDefinition = new FormComponent("substitutesComponentDetailsForm", windowDefinition, null, null);
    // windowDefinition.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
    // formDefinition.addComponent(new EntityComboBox("product", formDefinition, "product", null));
    // formDefinition.addComponent(new TextInputComponent("quantity", formDefinition, "quantity", null));
    //
    // windowDefinition.addComponent(formDefinition);
    //
    // windowDefinition.initialize();
    //
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createInstructionGridView() {
    // DataDefinition dataDefinition = dataDefinitionService.get("products", "instruction");
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", dataDefinition, "products.instructionGridView");
    // windowDefinition.addRawOption(new ComponentOption("backButton", ImmutableMap.of("value", "false")));
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.instructionGridView");
    // viewDefinition.setRoot(windowDefinition);
    // GridComponent grid = new GridComponent("instructionsGrid", windowDefinition, null, null);
    // grid.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
    // grid.addRawOption(new ComponentOption("correspondingView", ImmutableMap.of("value", "products.instructionDetailsView")));
    // grid.addRawOption(new ComponentOption("paging", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("sortable", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("filter", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("multiselect", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("height", ImmutableMap.of("value", "450")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "number", "value", "number")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value", "name")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "product", "value", "product", "expression",
    // "#product['name']")));
    // windowDefinition.addComponent(grid);
    // windowDefinition.initialize();
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createInstructionDetailsView() {
    // DataDefinition substituteDataDefinition = dataDefinitionService.get("products", "instruction");
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", substituteDataDefinition,
    // "products.instructionDetailsView");
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.instructionDetailsView");
    // viewDefinition.setRoot(windowDefinition);
    // FormComponent formDefinition = new FormComponent("detailsForm", windowDefinition, null, null);
    // windowDefinition.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
    // formDefinition.addComponent(new CheckBoxComponent("master", formDefinition, "master", null));
    // formDefinition.addComponent(new TextInputComponent("number", formDefinition, "number", null));
    // formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
    // formDefinition.addComponent(new EntityComboBox("product", formDefinition, "product", null));
    // formDefinition.addComponent(new DynamicComboBox("typeOfMaterial", formDefinition, "typeOfMaterial", null));
    // formDefinition.addComponent(new TextInputComponent("dateFrom", formDefinition, "dateFrom", null));
    // formDefinition.addComponent(new TextInputComponent("dateTo", formDefinition, "dateTo", null));
    // formDefinition.addComponent(new TextInputComponent("description", formDefinition, "description", null));
    // windowDefinition.addComponent(formDefinition);
    // windowDefinition.initialize();
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createOrderGridView() {
    // DataDefinition dataDefinition = dataDefinitionService.get("products", "order");
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", dataDefinition, "products.orderGridView");
    // windowDefinition.addRawOption(new ComponentOption("backButton", ImmutableMap.of("value", "false")));
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.orderGridView");
    // viewDefinition.setRoot(windowDefinition);
    // GridComponent grid = new GridComponent("instructionsGrid", windowDefinition, null, null);
    // grid.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
    // grid.addRawOption(new ComponentOption("correspondingView", ImmutableMap.of("value", "products.orderDetailsView")));
    // grid.addRawOption(new ComponentOption("paging", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("sortable", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("filter", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("multiselect", ImmutableMap.of("value", "true")));
    // grid.addRawOption(new ComponentOption("height", ImmutableMap.of("value", "450")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "number", "value", "number")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "name", "value", "name")));
    // grid.addRawOption(new ComponentOption("column", ImmutableMap.of("name", "state", "value", "state")));
    // windowDefinition.addComponent(grid);
    // windowDefinition.initialize();
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createOrderDetailsView() {
    // DataDefinition substituteDataDefinition = dataDefinitionService.get("products", "order");
    // WindowComponent windowDefinition = new WindowComponent("mainWindow", substituteDataDefinition,
    // "products.orderDetailsView");
    // ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.orderDetailsView");
    // viewDefinition.setRoot(windowDefinition);
    // viewDefinition.setViewHook(hookFactory.getHook("com.qcadoo.mes.products.ProductService", "afterOrderDetailsLoad"));
    // FormComponent formDefinition = new FormComponent("detailsForm", windowDefinition, null, null);
    // windowDefinition.addRawOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
    // formDefinition.addComponent(new TextInputComponent("number", formDefinition, "number", null));
    // formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
    // formDefinition.addComponent(new TextInputComponent("dateFrom", formDefinition, "dateFrom", null));
    // formDefinition.addComponent(new TextInputComponent("dateTo", formDefinition, "dateTo", null));
    // formDefinition.addComponent(new DynamicComboBox("state", formDefinition, "state", null));
    // formDefinition.addComponent(new DynamicComboBox("machine", formDefinition, "machine", null));
    // formDefinition.addComponent(new EntityComboBox("product", formDefinition, "product", null));
    //
    // formDefinition.addComponent(new TextInputComponent("defaultInstruction", formDefinition, "defaultInstruction", null));
    // formDefinition.addComponent(new EntityComboBox("instruction", formDefinition, "instruction",
    // "#{mainWindow.detailsForm.product}.instructions"));
    //
    // formDefinition.addComponent(new TextInputComponent("plannedQuantity", formDefinition, "plannedQuantity", null));
    // formDefinition.addComponent(new TextInputComponent("doneQuantity", formDefinition, "doneQuantity", null));
    // formDefinition.addComponent(new TextInputComponent("effectiveDateFrom", formDefinition, "effectiveDateFrom", null));
    // formDefinition.addComponent(new TextInputComponent("effectiveDateTo", formDefinition, "effectiveDateTo", null));
    // Component<?> startWorkerField = new TextInputComponent("startWorker", formDefinition, "startWorker", null);
    // ((AbstractComponent<?>) startWorkerField).setDefaultEnabled(false);
    // formDefinition.addComponent(startWorkerField);
    // formDefinition.addComponent(new TextInputComponent("endWorker", formDefinition, "endWorker", null));
    //
    // windowDefinition.addComponent(formDefinition);
    // windowDefinition.initialize();
    // return viewDefinition;
    // }

}
