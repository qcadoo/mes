package com.qcadoo.mes.view.components.tree;

import org.json.JSONException;
import org.json.JSONObject;

public class TreeDataType {

    private final String name;

    private String nodeLabelExpression;

    private String correspondingView;

    private String correspondingComponent;

    public TreeDataType(final String name) {
        this.name = name;
    }

    public TreeDataType(final String name, final String nodeLabelExpression, final String correspondingView,
            final String correspondingComponent) {
        this(name);
        this.nodeLabelExpression = nodeLabelExpression;
        this.correspondingView = correspondingView;
        this.correspondingComponent = correspondingComponent;
    }

    public String getName() {
        return name;
    }

    public String getNodeLabelExpression() {
        return nodeLabelExpression;
    }

    public String getCorrespondingView() {
        return correspondingView;
    }

    public String getCorrespondingComponent() {
        return correspondingComponent;
    }

    public void setNodeLabelExpression(final String nodeLabelExpression) {
        this.nodeLabelExpression = nodeLabelExpression;
    }

    public void setCorrespondingView(final String correspondingView) {
        this.correspondingView = correspondingView;
    }

    public void setCorrespondingComponent(final String correspondingComponent) {
        this.correspondingComponent = correspondingComponent;
    }

    public void setOption(final String type, final String value) {
        if ("nodeLabelExpression".equals(type)) {
            setNodeLabelExpression(value);
        } else if ("correspondingView".equals(type)) {
            setCorrespondingView(value);
        } else if ("correspondingComponent".equals(type)) {
            setCorrespondingComponent(value);
        } else {
            throw new IllegalStateException("Unknown tree 'dataType' option: " + type);
        }
    }

    public void validate() {
        if (nodeLabelExpression == null) {
            throw new IllegalStateException("Node 'dataType' of tree must contain 'nodeLabelExpression' option");
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("correspondingView", correspondingView);
        obj.put("correspondingComponent", correspondingComponent);
        return obj;
    }
}
