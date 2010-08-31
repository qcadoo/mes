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

    public ViewDefinition getViewDefinition(String viewName) {
        if ("products.productGridView".equals(viewName)) {
            return createProductGridView();
        } else if ("products.productDetailsView".equals(viewName)) {
            return createProductDetailsView();
        }
        return null;
    }

    private ViewDefinition createProductGridView() {
        ViewDefinition viewDefinition = new ViewDefinition("products.productGridView");

        List<ViewElementDefinition> elements = new LinkedList<ViewElementDefinition>();

        DataDefinition gridDataDefinition = dataDefinitionService.get("products.product");
        GridDefinition gridDefinition = new GridDefinition("products", gridDataDefinition);
        Map<String, String> gridOptions = new HashMap<String, String>();
        gridOptions.put("paging", "true");
        gridOptions.put("sortable", "true");
        gridOptions.put("filter", "true");
        gridOptions.put("multiselect", "true");
        gridOptions.put("height", "450");
        gridDefinition.setOptions(gridOptions);
        Map<String, String> gridEvents = new HashMap<String, String>();
        gridEvents.put("newClicked", "goTo(products.productDetailsView.html)");
        gridEvents.put("rowDblClicked", "goTo(products.productDetailsView.html?products.product={$rowId})");
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
        elements.add(form);

        DataDefinition substituteDataDefinition = dataDefinitionService.get("products.substitute");
        GridDefinition substituteGridDefinition = new GridDefinition("substitutesGrid", substituteDataDefinition);
        substituteGridDefinition.setParentDefinition(productDataDefinition);
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
        substituteGridEvents.put("onSelect", "#substitutesComponentGrid.setParent({$rowId})");
        substituteGridDefinition.setEvents(substituteGridEvents);
        elements.add(substituteGridDefinition);

        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        GridDefinition substituteComponentGridDefinition = new GridDefinition("substitutesComponentGrid",
                substituteComponentDataDefinition);
        substituteComponentGridDefinition.setParentDefinition(substituteDataDefinition);
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
        elements.add(substituteComponentGridDefinition);

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
