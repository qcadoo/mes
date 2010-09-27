package com.qcadoo.mes.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.DataDefinitionService;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.api.ViewDefinitionService;
import com.qcadoo.mes.core.enums.PluginStatus;
import com.qcadoo.mes.core.internal.hooks.HookFactory;
import com.qcadoo.mes.core.internal.view.ViewDefinitionImpl;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.view.Component;
import com.qcadoo.mes.core.view.ViewDefinition;
import com.qcadoo.mes.core.view.containers.FormComponent;
import com.qcadoo.mes.core.view.containers.WindowComponent;
import com.qcadoo.mes.core.view.elements.CheckBoxComponent;
import com.qcadoo.mes.core.view.elements.DynamicComboBox;
import com.qcadoo.mes.core.view.elements.EntityComboBox;
import com.qcadoo.mes.core.view.elements.GridComponent;
import com.qcadoo.mes.core.view.elements.LinkButton;
import com.qcadoo.mes.core.view.elements.TextInputComponent;
import com.qcadoo.mes.core.view.elements.grid.ColumnDefinition;

@Service
public final class ViewDefinitionServiceImpl implements ViewDefinitionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private HookFactory hookFactory;

    private Map<String, ViewDefinition> viewDefinitions;

    @Override
    public void initViews() {
        viewDefinitions = new HashMap<String, ViewDefinition>();
        viewDefinitions.put("products.productGridView", createProductGridView());
        viewDefinitions.put("products.productDetailsView", createProductDetailsView());
        viewDefinitions.put("products.substituteDetailsView", createProductSubstituteDetailsView());
        viewDefinitions.put("products.substituteComponentDetailsView", createProductSubstituteComponentDetailsView());

        viewDefinitions.put("products.grid", createTestGridView());
        viewDefinitions.put("products.form", createTestFormView());

        viewDefinitions.put("products.orderGridView", createOrderGridView());
        viewDefinitions.put("products.orderDetailsView", createOrderDetailsView());
        viewDefinitions.put("products.instructionGridView", createInstructionGridView());
        viewDefinitions.put("products.instructionDetailsView", createInstructionDetailsView());
        // viewDefinitions.put("users.groupGridView", createUserGroupGridView());
        // viewDefinitions.put("users.groupDetailsView", createUserGroupDetailsView());
        // viewDefinitions.put("users.userGridView", createUserGridView());
        // viewDefinitions.put("users.userDetailsView", createUserDetailsView());
        // viewDefinitions.put("core.dictionaryGridView", createDictionaryGridView());
        // viewDefinitions.put("core.dictionaryDetailsView", createDictionaryDetailsView());
        // viewDefinitions.put("core.dictionaryItemDetailsView", createDictionaryItemDetailsView());
        viewDefinitions.put("plugins.pluginGridView", createPluginGridView());
        viewDefinitions.put("plugins.pluginDetailsView", createPluginDetailsView());
    }

    @Override
    @Transactional(readOnly = true)
    public ViewDefinition get(final String pluginIdentifier, final String viewName) {
        ViewDefinition viewDefinition = viewDefinitions.get(viewName);
        PluginsPlugin plugin = pluginManagementService.getPluginByIdentifierAndStatus(pluginIdentifier,
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

    private ViewDefinitionImpl createTestGridView() {
        DataDefinition testADD = dataDefinitionService.get("products", "testBeanA");

        WindowComponent windowDefinition = new WindowComponent("mainWindow", testADD, "products.grid");
        windowDefinition.setBackButton(false);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.grid");
        viewDefinition.setRoot(windowDefinition);

        GridComponent grid = new GridComponent("beansAGrid", windowDefinition, null, null);
        grid.setCorrespondingViewName("products.form");
        grid.addOptions("paging", "true");
        grid.addOptions("sortable", "true");
        grid.addOptions("filter", "true");
        grid.addOptions("multiselect", "true");
        grid.addOptions("height", "450");
        ColumnDefinition columnName = createColumnDefinition("name", testADD.getField("name"), null);

        grid.addColumn(columnName);

        windowDefinition.addComponent(grid);

        windowDefinition.initialize();

        return viewDefinition;
    }

    private ViewDefinitionImpl createTestFormView() {
        DataDefinition testADD = dataDefinitionService.get("products", "testBeanA");
        DataDefinition testBDD = dataDefinitionService.get("products", "testBeanB");
        DataDefinition testCDD = dataDefinitionService.get("products", "testBeanC");

        WindowComponent windowDefinition = new WindowComponent("mainWindow", testADD, "products.form");

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.form");
        viewDefinition.setRoot(windowDefinition);

        FormComponent formDefinition = new FormComponent("beanAForm", windowDefinition, null, null);
        formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
        formDefinition.addComponent(new CheckBoxComponent("active", formDefinition, "active", null));
        formDefinition.addComponent(new EntityComboBox("beanB", formDefinition, "beanB", null));
        formDefinition.addComponent(new EntityComboBox("beanA", formDefinition, "beanA", "#{mainWindow.beanAForm.beanB}.beansA"));
        formDefinition.addComponent(new TextInputComponent("nameM", formDefinition, null, null));
        formDefinition.addComponent(new TextInputComponent("nameB", formDefinition, "beanB.name", null));
        formDefinition.addComponent(new TextInputComponent("nameC", formDefinition, "beanB.beanC.name", null));
        GridComponent beanAForm_beansCGrig = new GridComponent("beansCGrig", formDefinition, null, "beansC");
        beanAForm_beansCGrig.addColumn(createColumnDefinition("name", testCDD.getField("name"), null));
        formDefinition.addComponent(beanAForm_beansCGrig);
        windowDefinition.addComponent(formDefinition);

        GridComponent beansBGrig = new GridComponent("beansBGrig", windowDefinition, null,
                "#{mainWindow.beanAForm.beansCGrig}.beansB");
        beansBGrig.addColumn(createColumnDefinition("name", testBDD.getField("name"), null));
        windowDefinition.addComponent(beansBGrig);

        FormComponent formCDefinition = new FormComponent("beanCForm", windowDefinition, null, "#{mainWindow.beansBGrig}.beanC");
        formCDefinition.addComponent(new TextInputComponent("name", formCDefinition, "name", null));
        FormComponent formCDefinition_formA = new FormComponent("formA", formCDefinition, "beanA", null);
        formCDefinition_formA.addComponent(new TextInputComponent("name", formCDefinition_formA, "name", null));
        formCDefinition.addComponent(formCDefinition_formA);
        GridComponent beanCForm_beansBGrig = new GridComponent("beansBGrig", formCDefinition, null, "beansB");
        beanCForm_beansBGrig.addColumn(createColumnDefinition("name", testBDD.getField("name"), null));
        formCDefinition.addComponent(beanCForm_beansBGrig);
        formCDefinition.addComponent(new TextInputComponent("nameB", formCDefinition, null,
                "#{mainWindow.beanCForm.beansBGrig}.name"));
        windowDefinition.addComponent(formCDefinition);

        FormComponent formBDefinition = new FormComponent("beanBForm", windowDefinition, null, "#{mainWindow.beansBGrig}");
        formBDefinition.addComponent(new TextInputComponent("name", formBDefinition, "name", null));
        windowDefinition.addComponent(formBDefinition);

        windowDefinition.initialize();

        return viewDefinition;
    }

    private ViewDefinition createProductGridView() {
        DataDefinition productDataDefinition = dataDefinitionService.get("products", "product");

        WindowComponent windowDefinition = new WindowComponent("mainWindow", productDataDefinition, "products.productGridView");
        windowDefinition.setBackButton(false);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.productGridView");
        viewDefinition.setRoot(windowDefinition);

        GridComponent grid = new GridComponent("productsGrid", windowDefinition, null, null);
        grid.setHeader(false);
        grid.setCorrespondingViewName("products.productDetailsView");
        grid.addOptions("paging", "true");
        grid.addOptions("sortable", "true");
        grid.addOptions("filter", "true");
        grid.addOptions("multiselect", "true");
        grid.addOptions("height", "450");
        ColumnDefinition columnNumber = createColumnDefinition("number", productDataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", productDataDefinition.getField("name"), null);
        ColumnDefinition columnType = createColumnDefinition("typeOfMaterial", productDataDefinition.getField("typeOfMaterial"),
                null);
        ColumnDefinition columnEan = createColumnDefinition("ean", productDataDefinition.getField("ean"), null);
        grid.addColumn(columnNumber);
        grid.addColumn(columnName);
        grid.addColumn(columnType);
        grid.addColumn(columnEan);
        windowDefinition.addComponent(grid);

        windowDefinition.initialize();

        return viewDefinition;
    }

    private ViewDefinition createProductDetailsView() {
        DataDefinition productDataDefinition = dataDefinitionService.get("products", "product");
        DataDefinition substituteDataDefinition = dataDefinitionService.get("products", "substitute");
        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products", "substituteComponent");

        WindowComponent windowDefinition = new WindowComponent("mainWindow", productDataDefinition, "products.productDetailsView");

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.productDetailsView");
        viewDefinition.setRoot(windowDefinition);

        FormComponent formDefinition = new FormComponent("productDetailsForm", windowDefinition, null, null);
        formDefinition.setHeader(false);

        formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
        formDefinition.addComponent(new TextInputComponent("number", formDefinition, "number", null));
        formDefinition.addComponent(new DynamicComboBox("typeOfMaterial", formDefinition, "typeOfMaterial", null));
        formDefinition.addComponent(new TextInputComponent("ean", formDefinition, "ean", null));
        formDefinition.addComponent(new DynamicComboBox("category", formDefinition, "category", null));
        formDefinition.addComponent(new TextInputComponent("unit", formDefinition, "unit", null));
        windowDefinition.addComponent(formDefinition);

        GridComponent substituteGridDefinition = new GridComponent("substitutesGrid", windowDefinition, "substitutes", null);
        substituteGridDefinition.addColumn(createColumnDefinition("number", substituteDataDefinition.getField("number"), null));
        substituteGridDefinition.addColumn(createColumnDefinition("name", substituteDataDefinition.getField("name"), null));
        substituteGridDefinition
                .addColumn(createColumnDefinition("priority", substituteDataDefinition.getField("priority"), null));
        substituteGridDefinition.addOptions("paging", "false");
        substituteGridDefinition.addOptions("sortable", "false");
        substituteGridDefinition.addOptions("filter", "false");
        substituteGridDefinition.addOptions("multiselect", "false");
        substituteGridDefinition.addOptions("height", "150");
        substituteGridDefinition.setCorrespondingViewName("products.substituteDetailsView");
        windowDefinition.addComponent(substituteGridDefinition);

        GridComponent substituteComponentGridDefinition = new GridComponent("substitutesComponentGrid", windowDefinition, null,
                "#{mainWindow.substitutesGrid}.components");
        substituteComponentGridDefinition.addColumn(createColumnDefinition("number",
                substituteComponentDataDefinition.getField("product"), "#product['number']"));
        substituteComponentGridDefinition.addColumn(createColumnDefinition("name",
                substituteComponentDataDefinition.getField("product"), "#product['name']"));
        substituteComponentGridDefinition.addColumn(createColumnDefinition("quantity",
                substituteComponentDataDefinition.getField("quantity"), null));
        substituteComponentGridDefinition.addOptions("paging", "false");
        substituteComponentGridDefinition.addOptions("sortable", "false");
        substituteComponentGridDefinition.addOptions("filter", "false");
        substituteComponentGridDefinition.addOptions("multiselect", "false");
        substituteComponentGridDefinition.addOptions("height", "150");
        substituteComponentGridDefinition.setCorrespondingViewName("products.substituteComponentDetailsView");
        windowDefinition.addComponent(substituteComponentGridDefinition);

        windowDefinition.initialize();

        return viewDefinition;
    }

    private ViewDefinition createProductSubstituteDetailsView() {
        DataDefinition substituteDataDefinition = dataDefinitionService.get("products", "substitute");

        WindowComponent windowDefinition = new WindowComponent("mainWindow", substituteDataDefinition,
                "products.substituteDetailsView");

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.substituteDetailsView");
        viewDefinition.setRoot(windowDefinition);

        FormComponent formDefinition = new FormComponent("substitutesDetailsForm", windowDefinition, null, null);
        formDefinition.setHeader(false);
        formDefinition.addComponent(new TextInputComponent("number", formDefinition, "number", null));
        formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
        formDefinition.addComponent(new TextInputComponent("priority", formDefinition, "priority", null));
        formDefinition.addComponent(new TextInputComponent("effectiveDateFrom", formDefinition, "effectiveDateFrom", null));
        formDefinition.addComponent(new TextInputComponent("effectiveDateTo", formDefinition, "effectiveDateTo", null));

        windowDefinition.addComponent(formDefinition);

        windowDefinition.initialize();

        return viewDefinition;
    }

    private ViewDefinition createProductSubstituteComponentDetailsView() {
        DataDefinition substitutesComponentDataDefinition = dataDefinitionService.get("products", "substituteComponent");

        WindowComponent windowDefinition = new WindowComponent("mainWindow", substitutesComponentDataDefinition,
                "products.substituteComponentDetailsView");

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.substituteComponentDetailsView");
        viewDefinition.setRoot(windowDefinition);

        FormComponent formDefinition = new FormComponent("substitutesComponentDetailsForm", windowDefinition, null, null);
        formDefinition.setHeader(false);
        formDefinition.addComponent(new EntityComboBox("product", formDefinition, "product", null));
        formDefinition.addComponent(new TextInputComponent("quantity", formDefinition, "quantity", null));

        windowDefinition.addComponent(formDefinition);

        windowDefinition.initialize();

        return viewDefinition;
    }

    // private ViewDefinition createUserGroupGridView() {
    // ViewDefinition viewDefinition = new ViewDefinition("users.groupGridView", "users");
    // viewDefinition.setHeader("users.groupGridView.header");
    //
    // List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();
    //
    // DataDefinition gridDataDefinition = dataDefinitionService.get("users.group");
    // GridDefinition gridDefinition = new GridDefinition("groups", gridDataDefinition);
    // gridDefinition.setCorrespondingViewName("users.groupDetailsView");
    // Map<String, String> gridOptions = new HashMap<String, String>();
    // gridOptions.put("paging", "true");
    // gridOptions.put("sortable", "true");
    // gridOptions.put("filter", "false");
    // gridOptions.put("multiselect", "false");
    // gridOptions.put("height", "450");
    // gridOptions.put("canDelete", "false");
    // gridOptions.put("canNew", "false");
    // gridDefinition.setOptions(gridOptions);
    // ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);
    // ColumnDefinition columnRole = createColumnDefinition("role", gridDataDefinition.getField("role"), null);
    //
    // gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnName, columnRole }));
    // elements.add(gridDefinition);
    //
    // viewDefinition.setElements(elements);
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createUserGroupDetailsView() {
    // ViewDefinition viewDefinition = new ViewDefinition("users.groupDetailsView", "users");
    // viewDefinition.setHeader("users.groupDetailsView.header");
    // List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();
    //
    // DataDefinition groupDataDefinition = dataDefinitionService.get("users.group");
    // FormDefinition formDefinition = new FormDefinition("groupDetailsForm", groupDataDefinition);
    // formDefinition.setParent("entityId");
    // formDefinition.setCorrespondingViewName("users.groupGridView");
    //
    // FormFieldDefinition fieldName = createFieldDefinition("name", groupDataDefinition.getField("name"),
    // fieldControlFactory.stringControl());
    // FormFieldDefinition fieldDescription = createFieldDefinition("description", groupDataDefinition.getField("description"),
    // fieldControlFactory.textControl());
    // FormFieldDefinition fieldRole = createFieldDefinition("role", groupDataDefinition.getField("role"),
    // fieldControlFactory.stringControl());
    //
    // formDefinition.addField(fieldName);
    // formDefinition.addField(fieldDescription);
    // formDefinition.addField(fieldRole);
    //
    // elements.add(formDefinition);
    //
    // viewDefinition.setElements(elements);
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createUserGridView() {
    // ViewDefinition viewDefinition = new ViewDefinition("users.userGridView", "users");
    // viewDefinition.setHeader("users.userGridView.header");
    //
    // List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();
    //
    // DataDefinition gridDataDefinition = dataDefinitionService.get("users.user");
    // GridDefinition gridDefinition = new GridDefinition("users", gridDataDefinition);
    // gridDefinition.setCorrespondingViewName("users.userDetailsView");
    // Map<String, String> gridOptions = new HashMap<String, String>();
    // gridOptions.put("paging", "true");
    // gridOptions.put("sortable", "true");
    // gridOptions.put("filter", "false");
    // gridOptions.put("multiselect", "true");
    // gridOptions.put("height", "450");
    // gridDefinition.setOptions(gridOptions);
    // ColumnDefinition columnLogin = createColumnDefinition("login", gridDataDefinition.getField("userName"), null);
    // ColumnDefinition columnEmail = createColumnDefinition("email", gridDataDefinition.getField("email"), null);
    // ColumnDefinition columnFirstName = createColumnDefinition("firstName", gridDataDefinition.getField("firstName"), null);
    // ColumnDefinition columnLastName = createColumnDefinition("lastName", gridDataDefinition.getField("lastName"), null);
    // ColumnDefinition columnUserGroup = createColumnDefinition("userGroup", gridDataDefinition.getField("userGroup"),
    // "#userGroup['name']");
    //
    // gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnLogin, columnEmail, columnFirstName,
    // columnLastName, columnUserGroup }));
    // elements.add(gridDefinition);
    //
    // viewDefinition.setElements(elements);
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createUserDetailsView() {
    // ViewDefinition viewDefinition = new ViewDefinition("users.userDetailsView", "users");
    // viewDefinition.setHeader("users.userDetailsView.header");
    // List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();
    //
    // DataDefinition userDataDefinition = dataDefinitionService.get("users.user");
    // FormDefinition formDefinition = new FormDefinition("userDetailsForm", userDataDefinition);
    // formDefinition.setParent("entityId");
    // formDefinition.setCorrespondingViewName("users.userGridView");
    //
    // FormFieldDefinition fieldUserName = createFieldDefinition("userName", userDataDefinition.getField("userName"),
    // fieldControlFactory.stringControl());
    // FormFieldDefinition fieldUserGroup = createFieldDefinition("userGroup", userDataDefinition.getField("userGroup"),
    // fieldControlFactory.lookupControl());
    // FormFieldDefinition fieldEmail = createFieldDefinition("email", userDataDefinition.getField("email"),
    // fieldControlFactory.stringControl());
    // FormFieldDefinition fieldFirstName = createFieldDefinition("firstName", userDataDefinition.getField("firstName"),
    // fieldControlFactory.stringControl());
    // FormFieldDefinition fieldLastName = createFieldDefinition("lastName", userDataDefinition.getField("lastName"),
    // fieldControlFactory.stringControl());
    // FormFieldDefinition fieldDescription = createFieldDefinition("description", userDataDefinition.getField("description"),
    // fieldControlFactory.textControl());
    // FormFieldDefinition fieldPassword = createFieldDefinition("password", userDataDefinition.getField("password"),
    // fieldControlFactory.passwordControl());
    // FormFieldDefinition fieldPasswordConfirmation = createFieldDefinition("passwordConfirmation",
    // userDataDefinition.getField("password"), fieldControlFactory.passwordConfirmationControl());
    //
    // formDefinition.addField(fieldUserName);
    // formDefinition.addField(fieldUserGroup);
    // formDefinition.addField(fieldEmail);
    // formDefinition.addField(fieldFirstName);
    // formDefinition.addField(fieldLastName);
    // formDefinition.addField(fieldDescription);
    // formDefinition.addField(fieldPassword);
    // formDefinition.addField(fieldPasswordConfirmation);
    //
    // elements.add(formDefinition);
    //
    // viewDefinition.setElements(elements);
    // return viewDefinition;
    // }

    private ViewDefinition createInstructionGridView() {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "instruction");
        WindowComponent windowDefinition = new WindowComponent("mainWindow", dataDefinition, "products.instructionGridView");
        windowDefinition.setBackButton(false);
        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.instructionGridView");
        viewDefinition.setRoot(windowDefinition);
        GridComponent grid = new GridComponent("instructionsGrid", windowDefinition, null, null);
        grid.setHeader(false);
        grid.setCorrespondingViewName("products.instructionDetailsView");
        grid.addOptions("paging", "true");
        grid.addOptions("sortable", "true");
        grid.addOptions("filter", "true");
        grid.addOptions("multiselect", "true");
        grid.addOptions("height", "450");
        ColumnDefinition columnNumber = createColumnDefinition("number", dataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", dataDefinition.getField("name"), null);
        ColumnDefinition columnProductName = createColumnDefinition("product", dataDefinition.getField("product"),
                "#product['name']");
        grid.addColumn(columnNumber);
        grid.addColumn(columnName);
        grid.addColumn(columnProductName);
        windowDefinition.addComponent(grid);
        windowDefinition.initialize();
        return viewDefinition;
    }

    private ViewDefinition createInstructionDetailsView() {
        DataDefinition substituteDataDefinition = dataDefinitionService.get("products", "instruction");
        WindowComponent windowDefinition = new WindowComponent("mainWindow", substituteDataDefinition,
                "products.instructionDetailsView");
        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.instructionDetailsView");
        viewDefinition.setRoot(windowDefinition);
        FormComponent formDefinition = new FormComponent("detailsForm", windowDefinition, null, null);
        formDefinition.setHeader(false);
        formDefinition.addComponent(new CheckBoxComponent("master", formDefinition, "master", null));
        formDefinition.addComponent(new TextInputComponent("number", formDefinition, "number", null));
        formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
        formDefinition.addComponent(new EntityComboBox("product", formDefinition, "product", null));
        formDefinition.addComponent(new DynamicComboBox("typeOfMaterial", formDefinition, "typeOfMaterial", null));
        formDefinition.addComponent(new TextInputComponent("dateFrom", formDefinition, "dateFrom", null));
        formDefinition.addComponent(new TextInputComponent("dateTo", formDefinition, "dateTo", null));
        formDefinition.addComponent(new TextInputComponent("description", formDefinition, "description", null));
        windowDefinition.addComponent(formDefinition);
        windowDefinition.initialize();
        return viewDefinition;
    }

    private ViewDefinition createOrderGridView() {
        DataDefinition dataDefinition = dataDefinitionService.get("products", "order");
        WindowComponent windowDefinition = new WindowComponent("mainWindow", dataDefinition, "products.orderGridView");
        windowDefinition.setBackButton(false);
        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.orderGridView");
        viewDefinition.setRoot(windowDefinition);
        GridComponent grid = new GridComponent("instructionsGrid", windowDefinition, null, null);
        grid.setHeader(false);
        grid.setCorrespondingViewName("products.orderDetailsView");
        grid.addOptions("paging", "true");
        grid.addOptions("sortable", "true");
        grid.addOptions("filter", "true");
        grid.addOptions("multiselect", "true");
        grid.addOptions("height", "450");
        ColumnDefinition columnNumber = createColumnDefinition("number", dataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", dataDefinition.getField("name"), null);
        ColumnDefinition columnState = createColumnDefinition("state", dataDefinition.getField("state"), null);
        grid.addColumn(columnNumber);
        grid.addColumn(columnName);
        grid.addColumn(columnState);
        windowDefinition.addComponent(grid);
        windowDefinition.initialize();
        return viewDefinition;
    }

    private ViewDefinition createOrderDetailsView() {
        DataDefinition substituteDataDefinition = dataDefinitionService.get("products", "order");
        WindowComponent windowDefinition = new WindowComponent("mainWindow", substituteDataDefinition,
                "products.orderDetailsView");
        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("products", "products.orderDetailsView");
        viewDefinition.setRoot(windowDefinition);
        viewDefinition.setViewHook(hookFactory.getHook("com.qcadoo.mes.products.ProductService", "afterOrderDetailsLoad"));
        FormComponent formDefinition = new FormComponent("detailsForm", windowDefinition, null, null);
        formDefinition.setHeader(false);
        formDefinition.addComponent(new TextInputComponent("number", formDefinition, "number", null));
        formDefinition.addComponent(new TextInputComponent("name", formDefinition, "name", null));
        formDefinition.addComponent(new TextInputComponent("dateFrom", formDefinition, "dateFrom", null));
        formDefinition.addComponent(new TextInputComponent("dateTo", formDefinition, "dateTo", null));
        formDefinition.addComponent(new DynamicComboBox("state", formDefinition, "state", null));
        formDefinition.addComponent(new DynamicComboBox("machine", formDefinition, "machine", null));
        formDefinition.addComponent(new EntityComboBox("product", formDefinition, "product", null));

        formDefinition.addComponent(new TextInputComponent("defaultInstruction", formDefinition, "defaultInstruction", null));
        formDefinition.addComponent(new EntityComboBox("instruction", formDefinition, "instruction",
                "#{mainWindow.detailsForm.product}.instructions"));

        formDefinition.addComponent(new TextInputComponent("plannedQuantity", formDefinition, "plannedQuantity", null));
        formDefinition.addComponent(new TextInputComponent("doneQuantity", formDefinition, "doneQuantity", null));
        formDefinition.addComponent(new TextInputComponent("effectiveDateFrom", formDefinition, "effectiveDateFrom", null));
        formDefinition.addComponent(new TextInputComponent("effectiveDateTo", formDefinition, "effectiveDateTo", null));
        Component<?> startWorkerField = new TextInputComponent("startWorker", formDefinition, "startWorker", null);
        startWorkerField.setDefaultEnabled(false);
        formDefinition.addComponent(startWorkerField);
        formDefinition.addComponent(new TextInputComponent("endWorker", formDefinition, "endWorker", null));

        windowDefinition.addComponent(formDefinition);
        windowDefinition.initialize();
        return viewDefinition;
    }

    // private ViewDefinition createDictionaryGridView() {
    // ViewDefinition viewDefinition = new ViewDefinition("core.dictionaryGridView", "dictionaries");
    // viewDefinition.setHeader("core.dictionaryGridView.header");
    //
    // List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();
    // DataDefinition gridDataDefinition = dataDefinitionService.get("core.dictionary");
    // GridDefinition gridDefinition = new GridDefinition("distionaries", gridDataDefinition);
    // gridDefinition.setCorrespondingViewName("core.dictionaryDetailsView");
    // Map<String, String> gridOptions = new HashMap<String, String>();
    // gridOptions.put("paging", "true");
    // gridOptions.put("sortable", "true");
    // gridOptions.put("filter", "true");
    // gridOptions.put("multiselect", "false");
    // gridOptions.put("height", "450");
    // gridOptions.put("canDelete", "false");
    // gridOptions.put("canNew", "false");
    // gridDefinition.setOptions(gridOptions);
    // ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);
    //
    // gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnName }));
    // elements.add(gridDefinition);
    //
    // viewDefinition.setElements(elements);
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createDictionaryDetailsView() {
    // ViewDefinition viewDefinition = new ViewDefinition("core.dictionaryDetailsView", "dictionaries");
    // viewDefinition.setHeader("core.dictionaryDetailsView.header");
    // List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();
    //
    // DataDefinition gridDataDefinition = dataDefinitionService.get("core.dictionaryItem");
    // GridDefinition gridDefinition = new GridDefinition("dictionaryItems", gridDataDefinition);
    // gridDefinition.setParent("entityId");
    // gridDefinition.setParentField("dictionary");
    // gridDefinition.setCorrespondingViewName("core.dictionaryItemDetailsView");
    // Map<String, String> gridOptions = new HashMap<String, String>();
    // gridOptions.put("paging", "false");
    // gridOptions.put("sortable", "false");
    // gridOptions.put("filter", "false");
    // gridOptions.put("multiselect", "true");
    // gridOptions.put("height", "250");
    // gridDefinition.setOptions(gridOptions);
    // ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);
    // ColumnDefinition columnDescription = createColumnDefinition("description", gridDataDefinition.getField("description"),
    // null);
    // gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnName, columnDescription }));
    // elements.add(gridDefinition);
    //
    // viewDefinition.setElements(elements);
    // return viewDefinition;
    // }
    //
    // private ViewDefinition createDictionaryItemDetailsView() {
    // ViewDefinition viewDefinition = new ViewDefinition("core.dictionaryItemDetailsView", "dictionaries");
    // viewDefinition.setHeader("core.dictionaryItemDetailsView.header");
    // List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();
    //
    // DataDefinition dictionaryItemDataDefinition = dataDefinitionService.get("core.dictionaryItem");
    // FormDefinition formDefinition = new FormDefinition("dictionaryItemDetailsForm", dictionaryItemDataDefinition);
    // formDefinition.setParent("entityId");
    // formDefinition.setParentField("dictionary");
    // formDefinition.setCorrespondingViewName("core.dictionaryDetailsView");
    //
    // FormFieldDefinition fieldName = createFieldDefinition("name", dictionaryItemDataDefinition.getField("name"),
    // fieldControlFactory.stringControl());
    // FormFieldDefinition fieldDescription = createFieldDefinition("description",
    // dictionaryItemDataDefinition.getField("description"), fieldControlFactory.textControl());
    //
    // formDefinition.addField(fieldName);
    // formDefinition.addField(fieldDescription);
    //
    // elements.add(formDefinition);
    //
    // viewDefinition.setElements(elements);
    // return viewDefinition;
    // }

    private ViewDefinition createPluginGridView() {
        DataDefinition gridDataDefinition = dataDefinitionService.get("plugins", "plugin");

        WindowComponent windowDefinition = new WindowComponent("mainWindow", gridDataDefinition, "plugins.pluginGridView");
        windowDefinition.setBackButton(false);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("plugins", "plugins.pluginGridView");
        viewDefinition.setRoot(windowDefinition);

        GridComponent grid = new GridComponent("pluginsGrid", windowDefinition, null, null);
        grid.setHeader(false);
        grid.setCorrespondingViewName("plugins.pluginDetailsView");
        grid.addOptions("paging", "true");
        grid.addOptions("sortable", "true");
        grid.addOptions("filter", "true");
        grid.addOptions("multiselect", "false");
        grid.addOptions("height", "450");
        grid.addOptions("canDelete", "false");
        grid.addOptions("canNew", "false");

        ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);
        ColumnDefinition columnDescription = createColumnDefinition("description", gridDataDefinition.getField("description"),
                null);
        ColumnDefinition columnVersion = createColumnDefinition("version", gridDataDefinition.getField("version"), null);
        ColumnDefinition columnVendor = createColumnDefinition("vendor", gridDataDefinition.getField("vendor"), null);
        ColumnDefinition columnStatus = createColumnDefinition("status", gridDataDefinition.getField("status"), null);

        grid.addColumn(columnStatus);
        grid.addColumn(columnName);
        grid.addColumn(columnVersion);
        grid.addColumn(columnVendor);
        grid.addColumn(columnDescription);

        windowDefinition.addComponent(grid);

        windowDefinition.addComponent(new LinkButton("downloadButton", windowDefinition, "../download.html"));
        windowDefinition.addComponent(new LinkButton("removeButton", windowDefinition, "../remove.html",
                "#{mainWindow.pluginsGrid}"));
        windowDefinition.addComponent(new LinkButton("enableButton", windowDefinition, "../enable.html",
                "#{mainWindow.pluginsGrid}"));
        windowDefinition.addComponent(new LinkButton("disableButton", windowDefinition, "../disable.html",
                "#{mainWindow.pluginsGrid}"));
        windowDefinition.addComponent(new LinkButton("deinstallButton", windowDefinition, "../deinstall.html",
                "#{mainWindow.pluginsGrid}"));
        windowDefinition.addComponent(new LinkButton("updateButton", windowDefinition, "../update.html",
                "#{mainWindow.pluginsGrid}"));

        windowDefinition.initialize();

        return viewDefinition;
    }

    private ViewDefinition createPluginDetailsView() {

        DataDefinition pluginDataDefinition = dataDefinitionService.get("plugins", "plugin");

        WindowComponent windowDefinition = new WindowComponent("mainWindow", pluginDataDefinition, "plugins.pluginDetailsView");

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("plugins", "plugins.pluginDetailsView");
        viewDefinition.setRoot(windowDefinition);

        FormComponent formDefinition = new FormComponent("pluginDetailsForm", windowDefinition, null, null);
        formDefinition.setHeader(false);

        Component<?> nameField = new TextInputComponent("name", formDefinition, "name", null);
        nameField.setDefaultEnabled(false);
        formDefinition.addComponent(nameField);

        Component<?> versionField = new TextInputComponent("version", formDefinition, "version", null);
        versionField.setDefaultEnabled(false);
        formDefinition.addComponent(versionField);

        Component<?> vendorField = new TextInputComponent("vendor", formDefinition, "vendor", null);
        vendorField.setDefaultEnabled(false);
        formDefinition.addComponent(vendorField);
        // TODO KRNA textarea
        Component<?> descriptionField = new TextInputComponent("description", formDefinition, "description", null);
        descriptionField.setDefaultEnabled(false);
        formDefinition.addComponent(descriptionField);

        Component<?> statusField = new DynamicComboBox("status", formDefinition, "status", null);
        statusField.setDefaultEnabled(false);
        formDefinition.addComponent(statusField);

        Component<?> baseField = new CheckBoxComponent("base", formDefinition, "base", null);
        baseField.setDefaultEnabled(false);
        formDefinition.addComponent(baseField);

        windowDefinition.addComponent(formDefinition);

        windowDefinition.initialize();

        return viewDefinition;
    }

    private ColumnDefinition createColumnDefinition(final String name, final FieldDefinition field, final String expression) {
        ColumnDefinition columnDefinition = new ColumnDefinition(name);
        columnDefinition.setFields(Arrays.asList(new FieldDefinition[] { field }));
        columnDefinition.setExpression(expression);
        return columnDefinition;
    }

    @Override
    public void save(final ViewDefinition viewDefinition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(final String pluginIdentifier, final String viewName) {
        // TODO Auto-generated method stub

    }

}
