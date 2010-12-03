package com.qcadoo.mes.view.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.internal.EnumType;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("grid")
public class GridComponentPattern extends AbstractComponentPattern {

    private static final String JSP_PATH = "newComponents/grid.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.Grid";

    private final Set<String> searchableColumns = new HashSet<String>();

    private final Set<String> orderableColumns = new HashSet<String>();

    private final List<Column> columns = new ArrayList<Column>();

    private String correspondingView;

    private String correspondingComponent;

    private boolean paginable = true;

    private boolean deletable = true;

    private boolean creatable = true;

    private int height = 300;

    private int width = 300;

    private boolean lookup;

    public GridComponentPattern(final String name, final String fieldPath, final String sourceFieldPath,
            final ComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new GridComponentState(getScopeFieldDefinition(), columns);
    }

    @Override
    public String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    public String getJavaScriptFilePath() {
        return JS_PATH;
    }

    @Override
    public String getJavaScriptObjectName() {
        return JS_OBJECT;
    }

    @Override
    protected void initializeOptions() throws JSONException {
        parseOptions();
        addOptions();

        if (correspondingView != null && correspondingComponent == null) {
            throw new IllegalStateException("Missing correspondingComponent for grid");
        }
    }

    private void addOptions() throws JSONException {
        addStaticJavaScriptOption("paginable", paginable);
        addStaticJavaScriptOption("deletable", deletable);
        addStaticJavaScriptOption("creatable", creatable);
        addStaticJavaScriptOption("height", height);
        addStaticJavaScriptOption("width", width);
        addStaticJavaScriptOption("fullscreen", width == 0 || height == 0); // TODO masz
        addStaticJavaScriptOption("lookup", lookup);
        addStaticJavaScriptOption("correspondingView", correspondingView);
        addStaticJavaScriptOption("correspondingComponent", correspondingComponent);
        addStaticJavaScriptOption("prioritizable", getDataDefinition().isPrioritizable());
        addStaticJavaScriptOption("searchableColumns", new JSONArray(searchableColumns));
        addStaticJavaScriptOption("orderableColumns", new JSONArray(orderableColumns));

        if (getScopeFieldDefinition() != null) {
            addStaticJavaScriptOption("scopeFieldName", getScopeFieldDefinition().getName());
        } else {
            addStaticJavaScriptOption("scopeFieldName", null);
        }

        addColumnOptions();
    }

    private void addColumnOptions() throws JSONException {
        JSONObject jsonColumns = new JSONObject();

        for (Column column : columns) {
            JSONObject jsonColumn = new JSONObject();
            jsonColumn.put("name", column.getName()); // TODO masz i18n
            jsonColumn.put("link", column.isLink());
            jsonColumn.put("hidden", column.isHidden());
            jsonColumn.put("width", column.getWidth());
            jsonColumn.put("align", column.getAlign());
            if (column.getFilterValues() != null && !column.getFilterValues().isEmpty()) {
                JSONObject jsonFilterValues = new JSONObject();
                for (Map.Entry<String, String> filterValue : column.getFilterValues().entrySet()) {
                    jsonFilterValues.put(filterValue.getKey(), filterValue.getValue());
                }
                jsonColumn.put("filterValues", jsonFilterValues);
            }
            jsonColumns.put(column.getName(), jsonColumn);
        }

        addStaticJavaScriptOption("columns", jsonColumns);
    }

    private void parseOptions() {
        for (ComponentOption option : getOptions()) {
            if ("correspondingView".equals(option.getType())) {
                correspondingView = option.getValue();
            } else if ("correspondingComponent".equals(option.getType())) {
                correspondingComponent = option.getValue();
            } else if ("paginable".equals(option.getType())) {
                paginable = Boolean.parseBoolean(option.getValue());
            } else if ("creatable".equals(option.getType())) {
                creatable = Boolean.parseBoolean(option.getValue());
            } else if ("deletable".equals(option.getType())) {
                deletable = Boolean.parseBoolean(option.getValue());
            } else if ("height".equals(option.getType())) {
                height = Integer.parseInt(option.getValue());
            } else if ("width".equals(option.getType())) {
                width = Integer.parseInt(option.getValue());
            } else if ("fullscreen".equals(option.getType())) {
                width = 0;
                height = 0;
            } else if ("lookup".equals(option.getType())) {
                lookup = Boolean.parseBoolean(option.getValue());
            } else if ("searchable".equals(option.getType())) {
                searchableColumns.addAll(parseColumns(option.getValue()));
            } else if ("orderable".equals(option.getType())) {
                orderableColumns.addAll(parseColumns(option.getValue()));
            } else if ("column".equals(option.getType())) {
                parseColumnOption(option);
            } else {
                throw new IllegalStateException("Unknown option for grid: " + option.getType());
            }
        }
    }

    private void parseColumnOption(final ComponentOption option) {
        Column column = new Column(option.getAtrributeValue("name"));
        String fields = option.getAtrributeValue("fields");
        if (fields != null) {
            for (FieldDefinition field : parseFields(fields)) {
                column.addField(field);
            }
        }
        column.setExpression(option.getAtrributeValue("expression"));
        String width = option.getAtrributeValue("width");
        if (width != null) {
            column.setWidth(Integer.valueOf(width));
        }
        if (option.getAtrributeValue("link") != null) {
            column.setLink(Boolean.parseBoolean(option.getAtrributeValue("link")));
        }
        if (option.getAtrributeValue("hidden") != null) {
            column.setHidden(Boolean.parseBoolean(option.getAtrributeValue("hidden")));
        }
        columns.add(column);
    }

    private Set<String> parseColumns(final String columns) {
        Set<String> set = new HashSet<String>();
        for (String column : columns.split("\\s*,\\s*")) {
            set.add(column);
        }
        return set;
    }

    private Set<FieldDefinition> parseFields(final String fields) {
        Set<FieldDefinition> set = new HashSet<FieldDefinition>();
        for (String field : fields.split("\\s*,\\s*")) {
            set.add(getDataDefinition().getField(field));
        }
        return set;
    }

    public class Column {

        private final String name;

        private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

        private String expression;

        private Integer width = 100;

        private boolean link;

        private boolean hidden;

        public Column(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private void setWidth(final Integer width) {
            this.width = width;
        }

        private Integer getWidth() {
            return width;
        }

        private boolean isLink() {
            return link;
        }

        private void setLink(final boolean link) {
            this.link = link;
        }

        private boolean isHidden() {
            return hidden;
        }

        private String getAlign() {
            if (fields.size() == 1 && Number.class.isAssignableFrom(fields.get(0).getType().getType())) {
                return "right";
            } else {
                return "left";
            }
        }

        private Map<String, String> getFilterValues() {
            if (fields.size() == 1 && fields.get(0).getType() instanceof EnumType) {
                Map<String, String> values = new HashMap<String, String>();
                EnumType type = (EnumType) fields.get(0).getType();
                for (String value : type.values()) {
                    values.put(value, value); // TODO masz i18n
                }
                return values;
            } else {
                return null;
            }
        }

        private void setHidden(final boolean hidden) {
            this.hidden = hidden;
        }

        private void setExpression(final String expression) {
            this.expression = expression;
        }

        private void addField(final FieldDefinition field) {
            fields.add(field);
        }

        public String getValue(final Entity entity, final Locale locale) {
            if (StringUtils.hasText(expression)) {
                return ExpressionUtil.getValue(entity, expression, locale);
            } else {
                return ExpressionUtil.getValue(entity, fields, locale);
            }
        }

    }

}
