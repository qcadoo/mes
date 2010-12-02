package com.qcadoo.mes.view.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.internal.EnumType;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractContainerPattern;

@ViewComponent("grid")
public class GridComponentPattern extends AbstractContainerPattern {

    private final Set<String> searchableColumns = new HashSet<String>();

    private final Set<String> orderableColumns = new HashSet<String>();

    private final Map<String, Column> columns = new LinkedHashMap<String, Column>();

    private String correspondingView;

    private String correspondingComponent;

    private boolean paginable;

    private boolean deletable;

    private boolean creatable;

    private int height;

    private int width;

    private boolean lookup;

    public GridComponentPattern(final String name, final String fieldPath, final String sourceFieldPath,
            final ComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new GridComponentState(getScopeFieldDefinition(), columns.values());
    }

    public boolean isPaginable() {
        return paginable;
    }

    public boolean isCreatable() {
        return creatable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public boolean isLookup() {
        return lookup;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isFullscreen() {
        return width == 0 && height == 0;
    }

    public boolean isPrioritizable() {
        return getDataDefinition().isPrioritizable();
    }

    public String getCorrespondingComponent() {
        return correspondingComponent;
    }

    public String getCorrespondingView() {
        return correspondingView;
    }

    public String getScopeFieldName() {
        if (getScopeFieldDefinition() != null) {
            return getScopeFieldDefinition().getName();
        } else {
            return null;
        }
    }

    public Map<String, Column> getColumns() {
        return columns;
    }

    public Set<String> getSearchableColumns() {
        return searchableColumns;
    }

    public Set<String> getOrderableColumns() {
        return orderableColumns;
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
            return name; // TODO masz i18n
        }

        public void setWidth(final Integer width) {
            this.width = width;
        }

        public Integer getWidth() {
            return width;
        }

        public boolean isLink() {
            return link;
        }

        public void setLink(final boolean link) {
            this.link = link;
        }

        public boolean isHidden() {
            return hidden;
        }

        public String getAlign() {
            if (fields.size() == 1 && Number.class.isAssignableFrom(fields.get(0).getType().getType())) {
                return "right";
            } else {
                return "left";
            }
        }

        public Map<String, String> getFilterValues() {
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

        public void setHidden(final boolean hidden) {
            this.hidden = hidden;
        }

        public void setExpression(final String expression) {
            this.expression = expression;
        }

        public void addField(final FieldDefinition field) {
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
