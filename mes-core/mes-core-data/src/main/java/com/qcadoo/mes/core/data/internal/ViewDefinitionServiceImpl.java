package com.qcadoo.mes.core.data.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.ViewDefinitionService;
import com.qcadoo.mes.core.data.controls.FieldControl;
import com.qcadoo.mes.core.data.controls.FieldControlFactory;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.form.FormDefinition;
import com.qcadoo.mes.core.data.definition.form.FormFieldDefinition;
import com.qcadoo.mes.core.data.definition.grid.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.grid.GridDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.definition.view.ViewDefinition;

@Service
public final class ViewDefinitionServiceImpl implements ViewDefinitionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FieldControlFactory fieldControlFactory;

    private Map<String, ViewDefinition> viewDefinitions;

    @PostConstruct
    public void initViews() {
        viewDefinitions = new HashMap<String, ViewDefinition>();
        viewDefinitions.put("products.productGridView", createProductGridView());
        viewDefinitions.put("products.productDetailsView", createProductDetailsView());
        viewDefinitions.put("products.substituteDetailsView", createProductSubstituteDetailsView());
        viewDefinitions.put("products.substituteComponentDetailsView", createProductSubstituteComponentDetailsView());
        viewDefinitions.put("products.orderGridView", createOrderGridView());
        viewDefinitions.put("products.orderDetailsView", createOrderDetailsView());
        viewDefinitions.put("products.instructionGridView", createInstructionGridView());
        viewDefinitions.put("products.instructionDetailsView", createInstructionDetailsView());
        viewDefinitions.put("users.groupGridView", createUserGroupGridView());
        viewDefinitions.put("users.groupDetailsView", createUserGroupDetailsView());
        viewDefinitions.put("users.userGridView", createUserGridView());
        viewDefinitions.put("users.userDetailsView", createUserDetailsView());
        viewDefinitions.put("core.dictionaryGridView", createDictionaryGridView());
        viewDefinitions.put("core.dictionaryDetailsView", createDictionaryDetailsView());
        viewDefinitions.put("core.dictionaryItemDetailsView", createDictionaryItemDetailsView());
    }

    @Override
    public ViewDefinition getViewDefinition(final String viewName) {
        return viewDefinitions.get(viewName);
    }

    @Override
    public List<ViewDefinition> getAllViews() {
        List<ViewDefinition> viewsList = new ArrayList<ViewDefinition>(viewDefinitions.values());
        Collections.sort(viewsList, new Comparator<ViewDefinition>() {

            @Override
            public int compare(final ViewDefinition v1, final ViewDefinition v2) {
                return v1.getName().compareTo(v2.getName());
            }
        });
        return viewsList;

    }

    private ViewDefinition createProductGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.productGridView");
        viewDefinition.setHeader("products.productGridView.header");

        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("products.product");
        GridDefinition gridDefinition = new GridDefinition("productsGrid", gridDataDefinition);
        gridDefinition.setCorrespondingViewName("products.productDetailsView");
        Map<String, String> gridOptions = new HashMap<String, String>();
        gridOptions.put("paging", "true");
        gridOptions.put("sortable", "true");
        gridOptions.put("filter", "true");
        gridOptions.put("multiselect", "true");
        gridOptions.put("height", "450");
        gridDefinition.setOptions(gridOptions);
        Map<String, String> gridEvents = new HashMap<String, String>();
        gridDefinition.setEvents(gridEvents);
        ColumnDefinition columnNumber = createColumnDefinition("number", gridDataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);
        ColumnDefinition columnType = createColumnDefinition("typeOfMaterial", gridDataDefinition.getField("typeOfMaterial"),
                null);
        ColumnDefinition columnEan = createColumnDefinition("ean", gridDataDefinition.getField("ean"), null);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnType, columnEan }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createProductDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.productDetailsView");
        viewDefinition.setHeader("products.productDetailsView.header");

        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition productDataDefinition = dataDefinitionService.get("products.product");
        FormDefinition formDefinition = new FormDefinition("productDetailsForm", productDataDefinition);
        formDefinition.setParent("entityId");
        formDefinition.setCorrespondingViewName("products.productGridView");

        FormFieldDefinition fieldNumber = createFieldDefinition("number", productDataDefinition.getField("number"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldName = createFieldDefinition("name", productDataDefinition.getField("name"),
                fieldControlFactory.textControl());
        FormFieldDefinition fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                productDataDefinition.getField("typeOfMaterial"), fieldControlFactory.selectControl());
        FormFieldDefinition fieldEan = createFieldDefinition("ean", productDataDefinition.getField("ean"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldCategory = createFieldDefinition("category", productDataDefinition.getField("category"),
                fieldControlFactory.editableSelectControl());
        FormFieldDefinition fieldUnit = createFieldDefinition("unit", productDataDefinition.getField("unit"),
                fieldControlFactory.stringControl());

        formDefinition.addField(fieldNumber);
        formDefinition.addField(fieldName);
        formDefinition.addField(fieldTypeOfMaterial);
        formDefinition.addField(fieldEan);
        formDefinition.addField(fieldCategory);
        formDefinition.addField(fieldUnit);

        elements.add(formDefinition);

        DataDefinition substituteDataDefinition = dataDefinitionService.get("products.substitute");
        GridDefinition substituteGridDefinition = new GridDefinition("substitutesGrid", substituteDataDefinition);
        substituteGridDefinition.setParent("viewElement:productDetailsForm");
        substituteGridDefinition.setParentField("product");
        substituteGridDefinition.setHeader("products.productDetailsView.substitutesGrid.header");
        ColumnDefinition columnNumber = createColumnDefinition("number", substituteDataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", substituteDataDefinition.getField("name"), null);
        ColumnDefinition columnPriority = createColumnDefinition("priority", substituteDataDefinition.getField("priority"), null);
        substituteGridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnPriority, columnNumber, columnName }));
        Map<String, String> substituteGridOptions = new HashMap<String, String>();
        substituteGridOptions.put("paging", "false");
        substituteGridOptions.put("sortable", "false");
        substituteGridOptions.put("filter", "false");
        substituteGridOptions.put("multiselect", "false");
        substituteGridOptions.put("height", "150");
        substituteGridDefinition.setOptions(substituteGridOptions);
        Map<String, String> substituteGridEvents = new HashMap<String, String>();
        substituteGridDefinition.setEvents(substituteGridEvents);
        substituteGridDefinition.setCorrespondingViewName("products.substituteDetailsView");
        // substituteGridDefinition.setCorrespondingViewModal(true);
        elements.add(substituteGridDefinition);

        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        GridDefinition substituteComponentGridDefinition = new GridDefinition("substitutesComponentGrid",
                substituteComponentDataDefinition);
        substituteComponentGridDefinition.setParent("viewElement:substitutesGrid");
        substituteComponentGridDefinition.setParentField("substitute");
        substituteComponentGridDefinition.setHeader("products.productDetailsView.substitutesComponentGrid.header");
        ColumnDefinition columnSubstituteNumber = createColumnDefinition("number",
                substituteComponentDataDefinition.getField("product"), "#product['number']");
        ColumnDefinition columnProductName = createColumnDefinition("name",
                substituteComponentDataDefinition.getField("product"), "#product['name']");
        ColumnDefinition columnQuantity = createColumnDefinition("quantity",
                substituteComponentDataDefinition.getField("quantity"), null);
        substituteComponentGridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnSubstituteNumber,
                columnProductName, columnQuantity }));
        Map<String, String> substituteComponentGridOptions = new HashMap<String, String>();
        substituteComponentGridOptions.put("paging", "false");
        substituteComponentGridOptions.put("sortable", "false");
        substituteComponentGridOptions.put("filter", "false");
        substituteComponentGridOptions.put("multiselect", "false");
        substituteComponentGridOptions.put("height", "150");
        substituteComponentGridDefinition.setOptions(substituteComponentGridOptions);
        substituteComponentGridDefinition.setCorrespondingViewName("products.substituteComponentDetailsView");
        elements.add(substituteComponentGridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createProductSubstituteDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.substituteDetailsView");
        viewDefinition.setHeader("products.substituteDetailsView.header");
        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition substitutesDataDefinition = dataDefinitionService.get("products.substitute");
        FormDefinition formDefinition = new FormDefinition("substitutesDetailsForm", substitutesDataDefinition);
        formDefinition.setCorrespondingViewName("products.productDetailsView");
        formDefinition.setParent("entityId");
        formDefinition.setParentField("product");

        FormFieldDefinition fieldNumber = createFieldDefinition("number", substitutesDataDefinition.getField("number"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldName = createFieldDefinition("name", substitutesDataDefinition.getField("name"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldPriority = createFieldDefinition("priority", substitutesDataDefinition.getField("priority"),
                fieldControlFactory.displayControl());
        FormFieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom",
                substitutesDataDefinition.getField("effectiveDateFrom"), fieldControlFactory.dateTimeControl());
        FormFieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo",
                substitutesDataDefinition.getField("effectiveDateTo"), fieldControlFactory.dateTimeControl());

        formDefinition.addField(fieldPriority);
        formDefinition.addField(fieldNumber);
        formDefinition.addField(fieldName);
        formDefinition.addField(fieldEffectiveDateFrom);
        formDefinition.addField(fieldEffectiveDateTo);

        elements.add(formDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createProductSubstituteComponentDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.substituteComponentDetailsView");
        viewDefinition.setHeader("products.substituteComponentDetailsView.header");
        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition substitutesComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        FormDefinition formDefinition = new FormDefinition("substitutesComponentDetailsForm", substitutesComponentDataDefinition);
        formDefinition.setCorrespondingViewName("products.productDetailsView");
        formDefinition.setParent("entityId");
        formDefinition.setParentField("substitute");

        FormFieldDefinition fieldProduct = createFieldDefinition("product",
                substitutesComponentDataDefinition.getField("product"), fieldControlFactory.lookupControl());
        FormFieldDefinition fieldQuantity = createFieldDefinition("quantity",
                substitutesComponentDataDefinition.getField("quantity"), fieldControlFactory.decimalControl());

        formDefinition.addField(fieldProduct);
        formDefinition.addField(fieldQuantity);

        elements.add(formDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createUserGroupGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("users.groupGridView");
        viewDefinition.setHeader("users.groupGridView.header");

        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("users.group");
        GridDefinition gridDefinition = new GridDefinition("groups", gridDataDefinition);
        gridDefinition.setCorrespondingViewName("users.groupDetailsView");
        Map<String, String> gridOptions = new HashMap<String, String>();
        gridOptions.put("paging", "true");
        gridOptions.put("sortable", "true");
        gridOptions.put("filter", "false");
        gridOptions.put("multiselect", "true");
        gridOptions.put("height", "450");
        gridDefinition.setOptions(gridOptions);
        ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnName }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createUserGroupDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("users.groupDetailsView");
        viewDefinition.setHeader("users.groupDetailsView.header");
        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition groupDataDefinition = dataDefinitionService.get("users.group");
        FormDefinition formDefinition = new FormDefinition("groupDetailsForm", groupDataDefinition);
        formDefinition.setParent("entityId");
        formDefinition.setCorrespondingViewName("users.groupGridView");

        FormFieldDefinition fieldName = createFieldDefinition("name", groupDataDefinition.getField("name"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldDescription = createFieldDefinition("description", groupDataDefinition.getField("description"),
                fieldControlFactory.textControl());
        FormFieldDefinition fieldRole = createFieldDefinition("role", groupDataDefinition.getField("role"),
                fieldControlFactory.stringControl());

        formDefinition.addField(fieldName);
        formDefinition.addField(fieldDescription);
        formDefinition.addField(fieldRole);

        elements.add(formDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createUserGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("users.userGridView");
        viewDefinition.setHeader("users.userGridView.header");

        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("users.user");
        GridDefinition gridDefinition = new GridDefinition("users", gridDataDefinition);
        gridDefinition.setCorrespondingViewName("users.userDetailsView");
        Map<String, String> gridOptions = new HashMap<String, String>();
        gridOptions.put("paging", "true");
        gridOptions.put("sortable", "true");
        gridOptions.put("filter", "false");
        gridOptions.put("multiselect", "true");
        gridOptions.put("height", "450");
        gridDefinition.setOptions(gridOptions);
        ColumnDefinition columnLogin = createColumnDefinition("login", gridDataDefinition.getField("userName"), null);
        ColumnDefinition columnEmail = createColumnDefinition("email", gridDataDefinition.getField("email"), null);
        ColumnDefinition columnFirstName = createColumnDefinition("firstName", gridDataDefinition.getField("firstName"), null);
        ColumnDefinition columnLastName = createColumnDefinition("lastName", gridDataDefinition.getField("lastName"), null);
        ColumnDefinition columnUserGroup = createColumnDefinition("userGroup", gridDataDefinition.getField("userGroup"),
                "#userGroup['name']");

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnLogin, columnEmail, columnFirstName,
                columnLastName, columnUserGroup }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createUserDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("users.userDetailsView");
        viewDefinition.setHeader("users.userDetailsView.header");
        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition userDataDefinition = dataDefinitionService.get("users.user");
        FormDefinition formDefinition = new FormDefinition("userDetailsForm", userDataDefinition);
        formDefinition.setParent("entityId");
        formDefinition.setCorrespondingViewName("users.userGridView");

        FormFieldDefinition fieldUserName = createFieldDefinition("userName", userDataDefinition.getField("userName"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldUserGroup = createFieldDefinition("userGroup", userDataDefinition.getField("userGroup"),
                fieldControlFactory.lookupControl());
        FormFieldDefinition fieldEmail = createFieldDefinition("email", userDataDefinition.getField("email"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldFirstName = createFieldDefinition("firstName", userDataDefinition.getField("firstName"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldLastName = createFieldDefinition("lastName", userDataDefinition.getField("lastName"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldDescription = createFieldDefinition("description", userDataDefinition.getField("description"),
                fieldControlFactory.textControl());
        FormFieldDefinition fieldPassword = createFieldDefinition("password", userDataDefinition.getField("password"),
                fieldControlFactory.passwordControl());
        FormFieldDefinition fieldPasswordConfirmation = createFieldDefinition("passwordConfirmation",
                userDataDefinition.getField("password"), fieldControlFactory.passwordConfirmationControl());

        formDefinition.addField(fieldUserName);
        formDefinition.addField(fieldUserGroup);
        formDefinition.addField(fieldEmail);
        formDefinition.addField(fieldFirstName);
        formDefinition.addField(fieldLastName);
        formDefinition.addField(fieldDescription);
        formDefinition.addField(fieldPassword);
        formDefinition.addField(fieldPasswordConfirmation);

        elements.add(formDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createInstructionGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.instructionGridView");
        viewDefinition.setHeader("products.instructionGridView.header");

        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("products.instruction");
        GridDefinition gridDefinition = new GridDefinition("instructions", gridDataDefinition);
        gridDefinition.setCorrespondingViewName("products.instructionDetailsView");
        Map<String, String> gridOptions = new HashMap<String, String>();
        gridOptions.put("paging", "true");
        gridOptions.put("sortable", "true");
        gridOptions.put("filter", "true");
        gridOptions.put("multiselect", "true");
        gridOptions.put("height", "450");
        gridDefinition.setOptions(gridOptions);
        ColumnDefinition columnNumber = createColumnDefinition("number", gridDataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);
        ColumnDefinition columnProductName = createColumnDefinition("product", gridDataDefinition.getField("product"),
                "#product['name']");

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnProductName }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;

    }

    private ViewDefinition createOrderGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.orderGridView");
        viewDefinition.setHeader("products.orderGridView.header");

        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("products.order");
        GridDefinition gridDefinition = new GridDefinition("orders", gridDataDefinition);
        gridDefinition.setCorrespondingViewName("products.orderDetailsView");
        Map<String, String> gridOptions = new HashMap<String, String>();
        gridOptions.put("paging", "true");
        gridOptions.put("sortable", "true");
        gridOptions.put("filter", "true");
        gridOptions.put("multiselect", "true");
        gridOptions.put("height", "450");
        gridDefinition.setOptions(gridOptions);
        ColumnDefinition columnNumber = createColumnDefinition("number", gridDataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);
        ColumnDefinition columnState = createColumnDefinition("state", gridDataDefinition.getField("state"), null);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnState }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createOrderDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.orderDetailsView");
        viewDefinition.setHeader("products.orderDetailsView.header");
        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition orderDataDefinition = dataDefinitionService.get("products.order");
        FormDefinition formDefinition = new FormDefinition("orderDetailsForm", orderDataDefinition);
        formDefinition.setParent("entityId");
        formDefinition.setCorrespondingViewName("products.orderGridView");

        FormFieldDefinition fieldNumber = createFieldDefinition("number", orderDataDefinition.getField("number"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldName = createFieldDefinition("name", orderDataDefinition.getField("name"),
                fieldControlFactory.textControl());
        FormFieldDefinition fieldDateFrom = createFieldDefinition("dateFrom", orderDataDefinition.getField("dateFrom"),
                fieldControlFactory.dateControl());
        FormFieldDefinition fieldDateTo = createFieldDefinition("dateTo", orderDataDefinition.getField("dateTo"),
                fieldControlFactory.dateControl());
        FormFieldDefinition fieldState = createFieldDefinition("state", orderDataDefinition.getField("state"),
                fieldControlFactory.selectControl());
        FormFieldDefinition fieldMachine = createFieldDefinition("machine", orderDataDefinition.getField("machine"),
                fieldControlFactory.editableSelectControl());
        FormFieldDefinition fieldProduct = createFieldDefinition("product", orderDataDefinition.getField("product"),
                fieldControlFactory.lookupControl());
        FormFieldDefinition fieldDefaultInstruction = createFieldDefinition("defaultInstruction",
                orderDataDefinition.getField("defaultInstruction"), fieldControlFactory.displayControl());
        FormFieldDefinition fieldInstruction = createFieldDefinition("instruction", orderDataDefinition.getField("instruction"),
                fieldControlFactory.selectControl());
        FormFieldDefinition fieldPlannedQuantity = createFieldDefinition("plannedQuantity",
                orderDataDefinition.getField("plannedQuantity"), fieldControlFactory.decimalControl());
        FormFieldDefinition fieldDoneQuantity = createFieldDefinition("doneQuantity",
                orderDataDefinition.getField("doneQuantity"), fieldControlFactory.decimalControl());
        FormFieldDefinition fieldEffectiveDateFrom = createFieldDefinition("effectiveDateFrom",
                orderDataDefinition.getField("effectiveDateFrom"), fieldControlFactory.displayControl());
        FormFieldDefinition fieldEffectiveDateTo = createFieldDefinition("effectiveDateTo",
                orderDataDefinition.getField("effectiveDateTo"), fieldControlFactory.displayControl());
        FormFieldDefinition fieldStartWorker = createFieldDefinition("startWorker", orderDataDefinition.getField("startWorker"),
                fieldControlFactory.displayControl());
        FormFieldDefinition fieldEndWorker = createFieldDefinition("endWorker", orderDataDefinition.getField("endWorker"),
                fieldControlFactory.displayControl());

        formDefinition.addField(fieldNumber);
        formDefinition.addField(fieldName);
        formDefinition.addField(fieldDateFrom);
        formDefinition.addField(fieldDateTo);
        formDefinition.addField(fieldState);
        formDefinition.addField(fieldMachine);
        formDefinition.addField(fieldProduct);
        formDefinition.addField(fieldDefaultInstruction);
        formDefinition.addField(fieldInstruction);
        formDefinition.addField(fieldPlannedQuantity);
        formDefinition.addField(fieldDoneQuantity);
        formDefinition.addField(fieldEffectiveDateFrom);
        formDefinition.addField(fieldEffectiveDateTo);
        formDefinition.addField(fieldStartWorker);
        formDefinition.addField(fieldEndWorker);

        elements.add(formDefinition);

        viewDefinition.setElements(elements);

        return viewDefinition;
    }

    private ViewDefinition createInstructionDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.instructionDetailsView");
        viewDefinition.setHeader("products.instructionDetailsView.header");
        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition orderDataDefinition = dataDefinitionService.get("products.instruction");
        FormDefinition formDefinition = new FormDefinition("instructionDetailsForm", orderDataDefinition);
        formDefinition.setParent("entityId");
        formDefinition.setCorrespondingViewName("products.instructionGridView");

        FormFieldDefinition fieldMaster = createFieldDefinition("master", orderDataDefinition.getField("master"),
                fieldControlFactory.yesNoControl());
        FormFieldDefinition fieldNumber = createFieldDefinition("number", orderDataDefinition.getField("number"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldName = createFieldDefinition("name", orderDataDefinition.getField("name"),
                fieldControlFactory.textControl());
        FormFieldDefinition fieldProduct = createFieldDefinition("product", orderDataDefinition.getField("product"),
                fieldControlFactory.lookupControl());
        FormFieldDefinition fieldTypeOfMaterial = createFieldDefinition("typeOfMaterial",
                orderDataDefinition.getField("typeOfMaterial"), fieldControlFactory.selectControl());
        FormFieldDefinition fieldDateFrom = createFieldDefinition("dateFrom", orderDataDefinition.getField("dateFrom"),
                fieldControlFactory.dateTimeControl());
        FormFieldDefinition fieldDateTo = createFieldDefinition("dateTo", orderDataDefinition.getField("dateTo"),
                fieldControlFactory.dateTimeControl());
        FormFieldDefinition fieldDescription = createFieldDefinition("description", orderDataDefinition.getField("description"),
                fieldControlFactory.textControl());

        formDefinition.addField(fieldMaster);
        formDefinition.addField(fieldNumber);
        formDefinition.addField(fieldName);
        formDefinition.addField(fieldProduct);
        formDefinition.addField(fieldTypeOfMaterial);
        formDefinition.addField(fieldDateFrom);
        formDefinition.addField(fieldDateTo);
        formDefinition.addField(fieldDescription);

        elements.add(formDefinition);

        viewDefinition.setElements(elements);

        return viewDefinition;
    }

    private ViewDefinition createDictionaryGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("core.dictionaryGridView");
        viewDefinition.setHeader("core.dictionaryGridView.header");

        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();
        DataDefinition gridDataDefinition = dataDefinitionService.get("core.dictionary");
        GridDefinition gridDefinition = new GridDefinition("distionaries", gridDataDefinition);
        gridDefinition.setCorrespondingViewName("core.dictionaryDetailsView");
        Map<String, String> gridOptions = new HashMap<String, String>();
        gridOptions.put("paging", "true");
        gridOptions.put("sortable", "true");
        gridOptions.put("filter", "true");
        gridOptions.put("multiselect", "false");
        gridOptions.put("height", "450");
        gridOptions.put("canDelete", "false");
        gridOptions.put("canNew", "false");
        gridDefinition.setOptions(gridOptions);
        ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnName }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createDictionaryDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("core.dictionaryDetailsView");
        viewDefinition.setHeader("core.dictionaryDetailsView.header");
        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("core.dictionaryItem");
        GridDefinition gridDefinition = new GridDefinition("dictionaryItems", gridDataDefinition);
        gridDefinition.setParent("entityId");
        gridDefinition.setParentField("dictionary");
        gridDefinition.setCorrespondingViewName("core.dictionaryItemDetailsView");
        Map<String, String> gridOptions = new HashMap<String, String>();
        gridOptions.put("paging", "false");
        gridOptions.put("sortable", "false");
        gridOptions.put("filter", "false");
        gridOptions.put("multiselect", "true");
        gridOptions.put("height", "250");
        gridDefinition.setOptions(gridOptions);
        ColumnDefinition columnName = createColumnDefinition("name", gridDataDefinition.getField("name"), null);
        ColumnDefinition columnDescription = createColumnDefinition("description", gridDataDefinition.getField("description"),
                null);
        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnName, columnDescription }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createDictionaryItemDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("core.dictionaryItemDetailsView");
        viewDefinition.setHeader("core.dictionaryItemDetailsView.header");
        List<ComponentDefinition> elements = new LinkedList<ComponentDefinition>();

        DataDefinition dictionaryItemDataDefinition = dataDefinitionService.get("core.dictionaryItem");
        FormDefinition formDefinition = new FormDefinition("dictionaryItemDetailsForm", dictionaryItemDataDefinition);
        formDefinition.setParent("entityId");
        formDefinition.setParentField("dictionary");
        formDefinition.setCorrespondingViewName("core.dictionaryDetailsView");

        FormFieldDefinition fieldName = createFieldDefinition("name", dictionaryItemDataDefinition.getField("name"),
                fieldControlFactory.stringControl());
        FormFieldDefinition fieldDescription = createFieldDefinition("description",
                dictionaryItemDataDefinition.getField("description"), fieldControlFactory.textControl());

        formDefinition.addField(fieldName);
        formDefinition.addField(fieldDescription);

        elements.add(formDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ColumnDefinition createColumnDefinition(final String name, final DataFieldDefinition field, final String expression) {
        ColumnDefinition columnDefinition = new ColumnDefinition(name);
        columnDefinition.setFields(Arrays.asList(new DataFieldDefinition[] { field }));
        columnDefinition.setExpression(expression);
        return columnDefinition;
    }

    private FormFieldDefinition createFieldDefinition(final String name, final DataFieldDefinition dataField,
            final FieldControl control) {
        FormFieldDefinition field = new FormFieldDefinition(name);
        field.setControl(control);
        field.setDataField(dataField);
        return field;
    }

}
