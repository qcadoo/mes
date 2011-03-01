/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.view.components.layout;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

@ViewComponent("gridLayout")
public class GridLayoutPattern extends AbstractLayoutPattern {

    private static final String JS_OBJECT = "QCD.components.containers.layout.GridLayout";

    private static final String JSP_PATH = "containers/layout/gridLayout.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/containers/layout/gridLayout.js";

    private GridLayoutCell[][] cells;

    private boolean fixedRowHeight = true;

    public GridLayoutPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public void parse(final Node componentNode, final ViewDefinitionParser parser) {
        super.parse(componentNode, parser);

        Integer columns = getIntAttribute(componentNode, "columns", parser);
        Integer rows = getIntAttribute(componentNode, "rows", parser);

        fixedRowHeight = parser.getBooleanAttribute(componentNode, "fixedRowHeight", true);

        Preconditions.checkNotNull(columns, "columns nod definied");
        Preconditions.checkNotNull(rows, "rows nod definied");

        cells = new GridLayoutCell[rows][];
        for (int row = 0; row < cells.length; row++) {
            cells[row] = new GridLayoutCell[columns];
            for (int col = 0; col < cells[row].length; col++) {
                cells[row][col] = new GridLayoutCell();
            }
        }

        NodeList childNodes = componentNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Preconditions.checkState("layoutElement".equals(child.getNodeName()), "gridlayout can contains only layoutElements");
            Integer column = getIntAttribute(child, "column", parser);
            Integer row = getIntAttribute(child, "row", parser);

            GridLayoutCell cell = createGridLayoutCell(child, parser);

            insertCell(cell, column, row);
        }

        if (parser.getBooleanAttribute(componentNode, "hasBorders", true)) {
            updateBorders();
        }
    }

    private void updateBorders() {
        int colsNumber = cells[0].length;
        boolean[] bordersArray = new boolean[colsNumber];
        for (int i = 0; i < colsNumber; i++) {
            bordersArray[i] = false;
        }

        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                if (cells[row][col].getComponents() != null) {
                    bordersArray[col + cells[row][col].getColspan() - 1] = true;
                }
            }
        }

        // remove last border
        for (int i = colsNumber - 1; i >= 0; i--) {
            if (bordersArray[i]) {
                bordersArray[i] = false;
                break;
            }
        }

        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                if (bordersArray[col + cells[row][col].getColspan() - 1]) {
                    cells[row][col].setRightBorder(true);
                }
            }
        }
    }

    private GridLayoutCell createGridLayoutCell(final Node child, final ViewDefinitionParser parser) {

        Integer colspan = getIntAttribute(child, "width", parser);
        Integer rowspan = getIntAttribute(child, "height", parser);

        ComponentPattern elementComponent = null;
        NodeList elementComponentNodes = child.getChildNodes();
        GridLayoutCell cell = new GridLayoutCell();
        for (int elementComponentNodesIter = 0; elementComponentNodesIter < elementComponentNodes.getLength(); elementComponentNodesIter++) {
            Node elementComponentNode = elementComponentNodes.item(elementComponentNodesIter);
            if (elementComponentNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Preconditions.checkState("component".equals(elementComponentNode.getNodeName()),
                    "layoutElement can contains only components");
            elementComponent = parser.parseComponent(elementComponentNode, this);
            this.addChild(elementComponent);
            cell.addComponent(elementComponent);
        }
        if (colspan != null) {
            cell.setColspan(colspan);
        }
        if (rowspan != null) {
            cell.setRowspan(rowspan);
        }
        return cell;
    }

    private void insertCell(final GridLayoutCell cell, final int column, final int row) {
        Preconditions.checkState(column > 0, "column number less than zero");
        Preconditions.checkState(row > 0, "row number less than zero");
        Preconditions.checkState(column <= cells[0].length, "column number to large");
        Preconditions.checkState(row <= cells.length, "row number to large");
        Preconditions.checkState(column + cell.getColspan() - 1 <= cells[0].length, "width number to large");
        Preconditions.checkState(row + cell.getRowspan() - 1 <= cells.length, "height number to large");

        for (int rowIter = row; rowIter < row + cell.getRowspan(); rowIter++) {
            for (int colIter = column; colIter < column + cell.getColspan(); colIter++) {
                GridLayoutCell beforeCell = cells[rowIter - 1][colIter - 1];
                Preconditions.checkState(beforeCell.isAvailable(), "cell [" + rowIter + "x" + colIter + "] is not available");
                beforeCell.setAvailable(false);
            }
        }

        cells[row - 1][column - 1] = cell;
    }

    private Integer getIntAttribute(final Node node, final String attribute, final ViewDefinitionParser parser) {
        String valueStr = parser.getStringAttribute(node, attribute);
        if (valueStr == null) {
            return null;
        }
        return Integer.parseInt(valueStr);
    }

    @Override
    public final Map<String, Object> prepareView(final Locale locale) {
        Map<String, Object> model = super.prepareView(locale);
        model.put("cells", cells);
        return model;
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("colsNumber", cells[0].length);
        json.put("fixedRowHeight", fixedRowHeight);
        return json;
    }

    @Override
    public String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    public String getJsFilePath() {
        return JS_PATH;
    }

    @Override
    public String getJsObjectName() {
        return JS_OBJECT;
    }

}
