package com.qcadoo.mes.crud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.Component;
import com.qcadoo.mes.view.SaveableComponent;
import com.qcadoo.mes.view.SelectableComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewValue;

@Controller
public final class CrudController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}", method = RequestMethod.GET)
    public ModelAndView getView(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("crudView");

        Map<String, String> translationsMap = translationService.getCommonsTranslations(locale);

        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);
        mav.addObject("viewDefinition", viewDefinition);

        viewDefinition.updateTranslations(translationsMap, locale);

        mav.addObject("entityId", arguments.get("entityId"));
        mav.addObject("contextEntityId", arguments.get("contextEntityId"));
        mav.addObject("contextFieldName", arguments.get("contextFieldName"));

        mav.addObject("translationsMap", translationsMap);

        if (arguments.get("message") != null) {
            mav.addObject("message", arguments.get("message"));
            if (arguments.get("messageType") != null) {
                mav.addObject("messageType", arguments.get("messageType"));
            } else {
                mav.addObject("messageType", "info");
            }
        }

        return mav;
    }

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/data", method = RequestMethod.GET)
    @ResponseBody
    public Object getData(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);
        if (arguments.get("entityId") != null) {
            Entity entity = viewDefinition.getDataDefinition().get(Long.parseLong(arguments.get("entityId")));
            return viewDefinition.getValue(entity, new HashMap<String, Entity>(), null, "", false);
        } else {
            return viewDefinition.getValue(null, new HashMap<String, Entity>(), null, "", false);
        }
    }

    @InitBinder("jsonBody")
    public void initBinder(final WebDataBinder binder, final HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            // throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    // throw ex;
                }
            }
        }
        String body = stringBuilder.toString();
        ((StringBuilder) binder.getTarget()).append(body);
    }

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/dataUpdate", method = RequestMethod.POST)
    @ResponseBody
    public Object getDataUpdate(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        try {
            JSONObject jsonBody = new JSONObject(body.toString());
            String componentName = jsonBody.getString("componentName").replaceAll("-", ".");
            JSONObject jsonValues = jsonBody.getJSONObject("data");

            Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

            ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonValues);

            ViewValue<Object> newViewValue = viewDefinition.getValue(null, selectedEntities, viewValue, componentName, false);

            return newViewValue;
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/save", method = RequestMethod.POST)
    @ResponseBody
    public Object performSave(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        try {
            JSONObject jsonBody = new JSONObject(body.toString());
            String componentName = jsonBody.getString("componentName").replaceAll("-", ".");
            JSONObject jsonValues = jsonBody.getJSONObject("data");

            Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

            ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonValues);

            SaveableComponent saveableComponent = (SaveableComponent) viewDefinition.lookupComponent(componentName);

            Entity entity = saveableComponent.getSaveableEntity(viewValue);

            String contextFieldName = jsonBody.getString("contextFieldName");
            String contextEntityId = jsonBody.getString("contextEntityId");

            if (saveableComponent.isRelatedToMainEntity() && StringUtils.hasText(contextFieldName)
                    && StringUtils.hasText(contextEntityId)) {
                entity.setField(contextFieldName, Long.parseLong(contextEntityId));
            }

            entity = saveableComponent.getDataDefinition().save(entity);

            selectedEntities.put(componentName, entity);

            return viewDefinition.getValue(null, selectedEntities, viewValue, componentName, true);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/delete", method = RequestMethod.POST)
    @ResponseBody
    public Object performDelete(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        try {
            JSONObject jsonBody = new JSONObject(body.toString());
            String componentName = jsonBody.getString("componentName").replaceAll("-", ".");
            JSONObject jsonValues = jsonBody.getJSONObject("data");

            Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

            ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonValues);

            SelectableComponent selectableComponent = (SelectableComponent) viewDefinition.lookupComponent(componentName);

            Long id = selectableComponent.getSelectedEntityId(viewValue);

            if (id != null) {
                ((Component<?>) selectableComponent).getDataDefinition().delete(id);
                selectedEntities.remove(componentName);
            }

            return viewDefinition.getValue(null, selectedEntities, viewValue, componentName, true);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "page/{pluginIdentifier}/{viewName}/move", method = RequestMethod.POST)
    @ResponseBody
    public Object performMove(@PathVariable("pluginIdentifier") final String pluginIdentifier,
            @PathVariable("viewName") final String viewName, @RequestParam final Map<String, String> arguments,
            @ModelAttribute("jsonBody") final StringBuilder body) {
        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        try {
            JSONObject jsonBody = new JSONObject(body.toString());
            String componentName = jsonBody.getString("componentName").replaceAll("-", ".");
            JSONObject jsonValues = jsonBody.getJSONObject("data");
            Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

            ViewValue<Object> viewValue = viewDefinition.castValue(selectedEntities, jsonValues);

            if (StringUtils.hasText(arguments.get("offset"))) {
                int offset = Integer.valueOf(arguments.get("offset"));

                SelectableComponent selectableComponent = (SelectableComponent) viewDefinition.lookupComponent(componentName);

                Long id = selectableComponent.getSelectedEntityId(viewValue);

                if (id != null) {
                    ((Component<?>) selectableComponent).getDataDefinition().move(id, offset);
                }
            }

            return viewDefinition.getValue(null, selectedEntities, viewValue, componentName, true);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
