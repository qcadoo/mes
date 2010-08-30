package com.qcadoo.mes.core.data.internal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.ViewService;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FormDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.definition.ViewDefinition;
import com.qcadoo.mes.core.data.definition.ViewElementDefinition;

public class ViewServiceImpl implements ViewService {

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
        substituteGridDefinition.setParent(productDataDefinition);
        ColumnDefinition columnNumber = createColumnDefinition("number", substituteDataDefinition.getField("number"), null);
        ColumnDefinition columnName = createColumnDefinition("name", substituteDataDefinition.getField("name"), null);
        ColumnDefinition columnPriority = createColumnDefinition("priority", substituteDataDefinition.getField("priority"), null);
        substituteGridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnNumber, columnName, columnPriority }));
        elements.add(substituteGridDefinition);

        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        GridDefinition substituteComponentGridDefinition = new GridDefinition("substitutesComponentGrid",
                substituteDataDefinition);
        substituteComponentGridDefinition.setParent(substituteDataDefinition);
        ColumnDefinition columnSubstituteNumber = createColumnDefinition("number",
                substituteComponentDataDefinition.getField("number"), "fields['product'].fields['number']");
        ColumnDefinition columnProductName = createColumnDefinition("name", substituteComponentDataDefinition.getField("name"),
                "fields['product'].fields['name']");
        ColumnDefinition columnQuantity = createColumnDefinition("quantity",
                substituteComponentDataDefinition.getField("quantity"), null);
        substituteComponentGridDefinition.setColumns(Arrays.asList(new ColumnDefinition[] { columnSubstituteNumber,
                columnProductName, columnQuantity }));
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
