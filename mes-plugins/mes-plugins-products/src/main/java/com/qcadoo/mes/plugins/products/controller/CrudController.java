package com.qcadoo.mes.plugins.products.controller;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.ViewDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.grid.GridDefinition;
import com.qcadoo.mes.core.data.definition.view.ViewDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.internal.types.BelongsToFieldType;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.types.EnumeratedFieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;
import com.qcadoo.mes.plugins.products.data.EntityDataUtils;
import com.qcadoo.mes.plugins.products.data.ListData;
import com.qcadoo.mes.plugins.products.data.ListDataUtils;
import com.qcadoo.mes.plugins.products.translation.TranslationService;

@Controller
public class CrudController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private DataAccessService dataAccessService;

    @Autowired
    private TranslationService translationService;

    private static final Logger LOG = LoggerFactory.getLogger(CrudController.class);

    @RequestMapping(value = "page/{viewName}", method = RequestMethod.GET)
    public ModelAndView getView(@PathVariable("viewName") final String viewName,
            @RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("crudView");

        Map<String, String> translationsMap = translationService.getCommonsTranslations(locale);

        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        mav.addObject("viewDefinition", viewDefinition);
        translationService.updateTranslationsForViewDefinition(viewDefinition, translationsMap, locale);

        Map<String, Map<Long, String>> dictionaryValues = new HashMap<String, Map<Long, String>>();
        Map<String, String> viewElementsOptionsJson = new HashMap<String, String>();

        for (ComponentDefinition viewElement : viewDefinition.getElements()) {
            viewElementsOptionsJson.put(viewElement.getName(), CrudControllerUtils.generateJsonViewElementOptions(viewElement));
            for (Entry<String, DataFieldDefinition> fieldDefEntry : viewElement.getDataDefinition().getFields().entrySet()) {
                switch (fieldDefEntry.getValue().getType().getNumericType()) {
                    case FieldTypeFactory.NUMERIC_TYPE_BELONGS_TO:
                        BelongsToFieldType belongsToField = (BelongsToFieldType) fieldDefEntry.getValue().getType();
                        Map<Long, String> fieldOptions = belongsToField.lookup(null);
                        dictionaryValues.put(fieldDefEntry.getKey(), fieldOptions);
                        break;
                    case FieldTypeFactory.NUMERIC_TYPE_DICTIONARY:
                    case FieldTypeFactory.NUMERIC_TYPE_ENUM:
                        EnumeratedFieldType enumeratedField = (EnumeratedFieldType) fieldDefEntry.getValue().getType();
                        List<String> options = enumeratedField.values();
                        Map<Long, String> fieldOptionsMap = new HashMap<Long, String>();
                        Long key = (long) 0;
                        for (String option : options) {
                            fieldOptionsMap.put(key++, option);
                        }
                        dictionaryValues.put(fieldDefEntry.getKey(), fieldOptionsMap);
                        break;
                }
            }
        }
        mav.addObject("dictionaryValues", dictionaryValues);
        mav.addObject("viewElementsOptions", viewElementsOptionsJson);

        mav.addObject("entityId", arguments.get("entityId"));
        mav.addObject("contextEntityId", arguments.get("contextEntityId"));

        mav.addObject("translationsMap", translationsMap);

        return mav;
    }

    @RequestMapping(value = "page/{viewName}/{elementName}/list", method = RequestMethod.GET)
    @ResponseBody
    public ListData getGridData(@PathVariable("viewName") final String viewName,
            @PathVariable("elementName") final String elementName, @RequestParam final Map<String, String> arguments) {

        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        ComponentDefinition element = viewDefinition.getElementByName(elementName);

        GridDefinition gridDefinition = (GridDefinition) element;
        DataDefinition dataDefinition = gridDefinition.getDataDefinition();

        SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(dataDefinition);

        if (arguments.get("entityId") != null && gridDefinition.getParent() != null) {
            Long parentId = Long.parseLong(arguments.get("entityId"));
            DataFieldDefinition parentField = dataDefinition.getField(gridDefinition.getParentField());
            searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(parentField, parentId));
        }
        if (arguments.get("maxResults") != null) {
            int maxResults = Integer.parseInt(arguments.get("maxResults"));
            checkArgument(maxResults >= 0, "Max results must be greater or equals 0");
            searchCriteriaBuilder = searchCriteriaBuilder.withMaxResults(maxResults);
        }
        if (arguments.get("firstResult") != null) {
            int firstResult = Integer.parseInt(arguments.get("firstResult"));
            checkArgument(firstResult >= 0, "First result must be greater or equals 0");
            searchCriteriaBuilder = searchCriteriaBuilder.withFirstResult(firstResult);
        }
        if (arguments.get("sortField") != null && arguments.get("sortOrder") != null) {
            if ("desc".equals(arguments.get("sortOrder"))) {
                searchCriteriaBuilder = searchCriteriaBuilder.orderBy(Order.desc(arguments.get("sortField")));
            } else {
                searchCriteriaBuilder = searchCriteriaBuilder.orderBy(Order.asc(arguments.get("sortField")));
            }
        }

        for (int i = 0;; i++) {
            if (arguments.get("filterObject[" + i + "][fieldName]") == null) {
                break;
            }
            String fieldName = arguments.get("filterObject[" + i + "][fieldName]");
            String operator = arguments.get("filterObject[" + i + "][operator]");
            String value = arguments.get("filterObject[" + i + "][filterValue]");

            DataFieldDefinition field = dataDefinition.getField(fieldName);
            if ("=".equals(operator)) {
                searchCriteriaBuilder.restrictedWith(Restrictions.eq(field, value));
            } else if ("<".equals(operator)) {
                searchCriteriaBuilder.restrictedWith(Restrictions.lt(field, value));
            } else if (">".equals(operator)) {
                searchCriteriaBuilder.restrictedWith(Restrictions.gt(field, value));
            } else if ("<=".equals(operator)) {
                searchCriteriaBuilder.restrictedWith(Restrictions.le(field, value));
            } else if (">=".equals(operator)) {
                searchCriteriaBuilder.restrictedWith(Restrictions.ge(field, value));
            } else if ("<>".equals(operator)) {
                searchCriteriaBuilder.restrictedWith(Restrictions.ne(field, value));
            } else if ("null".equals(operator)) {
                searchCriteriaBuilder.restrictedWith(Restrictions.isNull(field));
            } else if ("not null".equals(operator)) {
                searchCriteriaBuilder.restrictedWith(Restrictions.isNotNull(field));
            }
        }

        SearchCriteria searchCriteria = searchCriteriaBuilder.build();

        SearchResult rs = dataAccessService.find(searchCriteria);

        return ListDataUtils.generateListData(rs, gridDefinition);
    }

    @RequestMapping(value = "page/{viewName}/{elementName}/entity", method = RequestMethod.GET)
    @ResponseBody
    public Entity getEntityData(@PathVariable("viewName") final String viewName,
            @PathVariable("elementName") final String elementName, @RequestParam final Map<String, String> arguments) {

        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        ComponentDefinition element = viewDefinition.getElementByName(elementName);

        Entity entity = dataAccessService.get(element.getDataDefinition(), Long.parseLong(arguments.get("entityId")));

        return EntityDataUtils.generateEntityData(entity, element.getDataDefinition());
    }

    @RequestMapping(value = "page/{viewName}/{elementName}/save", method = RequestMethod.POST)
    @ResponseBody
    public ValidationResults saveEntity(@PathVariable("viewName") final String viewName,
            @PathVariable("elementName") final String elementName, @ModelAttribute final Entity entity, final Locale locale) {
        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        ComponentDefinition element = viewDefinition.getElementByName(elementName);

        ValidationResults validationResult = dataAccessService.save(element.getDataDefinition(), entity);

        translationService.translateValidationResults(validationResult, locale);

        return EntityDataUtils.generateValidationResultWithEntityData(validationResult, element.getDataDefinition());
    }

    @RequestMapping(value = "page/{viewName}/{elementName}/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteEntities(@PathVariable("viewName") final String viewName,
            @PathVariable("elementName") final String elementName, @RequestBody final List<Integer> selectedRows) {
        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        ComponentDefinition element = viewDefinition.getElementByName(elementName);

        if (selectedRows.size() > 0) {
            Long[] entitiesId = new Long[selectedRows.size()];
            int i = 0;
            for (Integer selectedRowId : selectedRows) {
                entitiesId[i++] = new Long(selectedRowId);
            }
            dataAccessService.delete(element.getDataDefinition(), entitiesId);
        }
        return "ok";
    }

    @RequestMapping(value = "page/{viewName}/{elementName}/move", method = RequestMethod.POST)
    @ResponseBody
    public String moveEntities(@PathVariable("viewName") final String viewName,
            @PathVariable("elementName") final String elementName, @RequestParam final Integer entityId,
            @RequestParam final Integer direction) {
        ViewDefinition viewDefinition = viewDefinitionService.getViewDefinition(viewName);
        ComponentDefinition element = viewDefinition.getElementByName(elementName);

        dataAccessService.move(element.getDataDefinition(), new Long(entityId), direction);

        return "ok";
    }
}
