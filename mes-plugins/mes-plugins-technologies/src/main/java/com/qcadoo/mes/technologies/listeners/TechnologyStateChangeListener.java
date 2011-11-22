package com.qcadoo.mes.technologies.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.MessageHolder;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeNotifierService.StateChangeListener;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginAccessor;

@Component
public class TechnologyStateChangeListener implements StateChangeListener {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginAccessor pluginAccessor;

    @Override
    public List<MessageHolder> onStateChange(Entity technology, TechnologyState newState) {
        List<MessageHolder> resultMessages = Lists.newArrayList();
        switch (newState) {
            case OUTDATED:
                if (isTechnologyUsedInActiveOrder(technology)) {
                    resultMessages.add(MessageHolder.error("technologies.technology.state.error.orderInProgress"));
                }
                break;
            default:
                break;
        }
        return resultMessages;
    }

    private boolean isTechnologyUsedInActiveOrder(final Entity technology) {
        if (!ordersPluginIsEnabled()) {
            return false;
        }
        SearchCriteriaBuilder searchCriteria = getOrderDataDefinition().find();
        searchCriteria.add(SearchRestrictions.belongsTo("technology", technology));
        searchCriteria.add(SearchRestrictions.in("state",
                Lists.newArrayList("01pending", "02accepted", "03inProgress", "06interrupted")));
        searchCriteria.setMaxResults(1);
        return searchCriteria.uniqueResult() != null;
    }

    private boolean ordersPluginIsEnabled() {
        return pluginAccessor.getPlugin("orders") != null;
    }

    private DataDefinition getOrderDataDefinition() {
        return dataDefinitionService.get("orders", "order");
    }

}
