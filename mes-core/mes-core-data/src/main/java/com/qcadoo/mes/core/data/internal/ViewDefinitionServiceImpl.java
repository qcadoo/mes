package com.qcadoo.mes.core.data.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.ViewDefinitionService;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FormDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.definition.ViewDefinition;
import com.qcadoo.mes.core.data.definition.ViewElementDefinition;

@Service
public class ViewDefinitionServiceImpl implements ViewDefinitionService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private Map<String, ViewDefinition> viewDefinitions = new HashMap<String, ViewDefinition>();

    @Override
    public ViewDefinition getViewDefinition(final String viewName) {
        ViewDefinition viewDefinition = viewDefinitions.get(viewName);
        if (viewDefinition == null) {
            if ("products.productGridView".equals(viewName)) {
                viewDefinition = createProductGridView();
            } else if ("products.productDetailsView".equals(viewName)) {
                viewDefinition = createProductDetailsView();
            } else if ("products.substituteDetailsView".equals(viewName)) {
                viewDefinition = createProductSubstituteDetailsView();
            } else if ("products.substituteComponentDetailsView".equals(viewName)) {
                viewDefinition = createProductSubstituteComponentDetailsView();
            } else if ("users.groupGridView".equals(viewName)) {
                viewDefinition = createUserGroupGridView();
            } else if ("users.groupDetailsView".equals(viewName)) {
                viewDefinition = createUserGroupDetailsView();
            } else if ("users.userGridView".equals(viewName)) {
                viewDefinition = createUserGridView();
            } else if ("users.userDetailsView".equals(viewName)) {
                viewDefinition = createUserDetailsView();
            } else if ("orders.orderGridView".equals(viewName)) {
                viewDefinition = createOrderGridView();
            }
            if (viewDefinition != null) {
                viewDefinitions.put(viewName, viewDefinition);
            }
        }
        return viewDefinition;
    }

    private ViewDefinition createProductGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.productGridView");

        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("products.product");
        GridDefinition gridDefinition = new GridDefinition("products", gridDataDefinition);
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

        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

        DataDefinition productDataDefinition = dataDefinitionService.get("products.product");
        FormDefinition form = new FormDefinition("productDetailsForm", productDataDefinition);
        form.setParent("entityId");
        form.setCorrespondingViewName("products.productGridView");
        elements.add(form);

        DataDefinition substituteDataDefinition = dataDefinitionService.get("products.substitute");
        GridDefinition substituteGridDefinition = new GridDefinition("substitutesGrid", substituteDataDefinition);
        substituteGridDefinition.setParent("viewElement:productDetailsForm");
        substituteGridDefinition.setParentField("product");
        ColumnDefinition columnNumber = createColumnDefinition("number", substituteDataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", substituteDataDefinition.getField("name"), null);
        ColumnDefinition columnPriority = createColumnDefinition("priority", substituteDataDefinition.getField("priority"), null);
        substituteGridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnPriority }));
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
        ColumnDefinition columnSubstituteNumber = createColumnDefinition("number",
                substituteComponentDataDefinition.getField("number"), "fields['product'].fields['number']");
        ColumnDefinition columnProductName = createColumnDefinition("name", substituteComponentDataDefinition.getField("name"),
                "fields['product'].fields['name']");
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
        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

        DataDefinition substitutesDataDefinition = dataDefinitionService.get("products.substitute");
        FormDefinition form = new FormDefinition("substitutesDetailsForm", substitutesDataDefinition);
        form.setCorrespondingViewName("products.productDetailsView");
        form.setParent("entityId");
        form.setParentField("product");
        elements.add(form);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createProductSubstituteComponentDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.substituteComponentDetailsView");
        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

        DataDefinition substitutesComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        FormDefinition form = new FormDefinition("substitutesComponentDetailsForm", substitutesComponentDataDefinition);
        form.setCorrespondingViewName("products.productDetailsView");
        form.setParent("entityId");
        form.setParentField("substitute");
        elements.add(form);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createUserGroupGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("users.groupGridView");

        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

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
        ColumnDefinition columnRole = createColumnDefinition("role", gridDataDefinition.getField("role"), null);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnName, columnRole }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createUserGroupDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("users.groupDetailsView");
        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

        DataDefinition groupDataDefinition = dataDefinitionService.get("users.group");
        FormDefinition form = new FormDefinition("groupDetailsForm", groupDataDefinition);
        form.setParent("entityId");
        form.setCorrespondingViewName("users.groupGridView");
        elements.add(form);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createUserGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("users.userGridView");

        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

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
        ColumnDefinition columnLogin = createColumnDefinition("login", gridDataDefinition.getField("login"), null);
        ColumnDefinition columnEmail = createColumnDefinition("email", gridDataDefinition.getField("email"), null);
        ColumnDefinition columnFirstName = createColumnDefinition("firstName", gridDataDefinition.getField("firstName"), null);
        ColumnDefinition columnLastName = createColumnDefinition("lastName", gridDataDefinition.getField("lastName"), null);
        ColumnDefinition columnUserGroup = createColumnDefinition("userGroup", gridDataDefinition.getField("userGroup"), null);

        gridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnLogin, columnEmail, columnFirstName,
                columnLastName, columnUserGroup }));
        elements.add(gridDefinition);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createUserDetailsView() {
        ViewDefinition viewDefinition = new ViewDefinition("users.userDetailsView");
        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

        DataDefinition userDataDefinition = dataDefinitionService.get("users.user");
        FormDefinition form = new FormDefinition("userDetailsForm", userDataDefinition);
        form.setParent("entityId");
        form.setCorrespondingViewName("users.userGridView");
        elements.add(form);

        viewDefinition.setElements(elements);
        return viewDefinition;
    }

    private ViewDefinition createOrderGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("orders.orderGridView");

        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("orders.order");
        GridDefinition gridDefinition = new GridDefinition("orders", gridDataDefinition);
        gridDefinition.setCorrespondingViewName("orders.orderDetailsView");
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

    private ColumnDefinition createColumnDefinition(final String name, final FieldDefinition field, final String expression) {
        ColumnDefinition columnDefinition = new ColumnDefinition(name);
        columnDefinition.setFields(Arrays.asList(new FieldDefinition[] { field }));
        columnDefinition.setExpression(expression);
        return columnDefinition;
    }

}
