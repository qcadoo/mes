package com.qcadoo.mes.newview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

@Service
public class ViewComponentsResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ViewComponentsResolver.class);

    private Map<String, Class<? extends ComponentPattern>> viewComponentClasses = new HashMap<String, Class<? extends ComponentPattern>>();

    @SuppressWarnings("unchecked")
    public void refreshAvaliebleComponentsList() {

        LOG.info("Scan for view components...");

        ComponentClassScanner scn = new ComponentClassScanner();
        scn.addIncludeFilter(new AnnotationTypeFilter(ViewComponent.class));

        // TODO mina change package scope
        Collection<Class<?>> componentsCollection = scn.getComponentClasses("com");

        for (Object clazzObj : componentsCollection) {
            Class<? extends ComponentPattern> clazz = (Class<? extends ComponentPattern>) clazzObj;
            ViewComponent viewComponentAnnotation = clazz.getAnnotation(ViewComponent.class);
            String componentName = viewComponentAnnotation.value();
            LOG.info("found Component '" + componentName + "' - " + clazz.getCanonicalName());

            viewComponentClasses.put(componentName, clazz);
        }
        LOG.info("Found " + viewComponentClasses.size() + " view components.");
    }

    public boolean hasViewComponent(String componentName) {
        return viewComponentClasses.containsKey(componentName);
    }

    public Set<String> getAvailableComponentsSet() {
        return viewComponentClasses.keySet();
    }

    public Class<? extends ComponentPattern> getViewComponentClass(String componentName) {
        return viewComponentClasses.get(componentName);
    }

    private class ComponentClassScanner extends ClassPathScanningCandidateComponentProvider {

        public ComponentClassScanner() {
            super(false);
        }

        @SuppressWarnings("rawtypes")
        public final Collection getComponentClasses(String basePackage) {
            basePackage = basePackage == null ? "" : basePackage;
            List<Class> classes = new ArrayList<Class>();
            for (BeanDefinition candidate : findCandidateComponents(basePackage)) {
                try {
                    Class cls = ClassUtils.resolveClassName(candidate.getBeanClassName(), ClassUtils.getDefaultClassLoader());
                    classes.add((Class) cls);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            return classes;
        }

    }

}
