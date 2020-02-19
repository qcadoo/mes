/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.productionPerShift;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// TODO maku - good candidate for a common service. Consider superclass/bean factory extraction.
@Service("balanceContextsCleanUpService")
public class BalanceContextsCleanUpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceContextsCleanUpService.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @RunIfEnabled(ProductionPerShiftConstants.PLUGIN_IDENTIFIER)
    public void cleanUp() {
        info("Starting clean up.");
        Collection<Long> ids = getContextIds();
        if (ids.isEmpty()) {
            info("There is no entities to be cleaned up. Aborting.");
            return;
        }
        int idsSize = ids.size();
        info("Found %s entities to delete.", idsSize);
        // FIXME maku - we need to introduce some kind of batch deletion..
        EntityOpResult deleteResult = getDataDefinition().delete(ids.toArray(new Long[idsSize]));
        if (deleteResult.isSuccessfull()) {
            info("Successfully deleted %s entities.", idsSize);
        } else {
            logFailure(deleteResult.getMessagesHolder());
        }
    }

    private Collection<Long> getContextIds() {
        DataDefinition dataDef = getDataDefinition();
        SearchCriteriaBuilder scb = dataDef.find();
        scb.setProjection(SearchProjections.alias(SearchProjections.id(), "id"));
        List<Long> ids = Lists.newLinkedList();
        for (Entity idProjection : scb.list().getEntities()) {
            ids.add((Long) idProjection.getField("id"));
        }
        return ids;
    }

    private DataDefinition getDataDefinition() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_BALANCE_CONTEXT);
    }

    private void logFailure(final EntityMessagesHolder messagesHolder) {
        StringBuilder errors = new StringBuilder();
        if (!messagesHolder.getGlobalErrors().isEmpty()) {
            errors.append("\nGlobal errors:\n\t");
            errors.append(StringUtils.join(messagesHolder.getGlobalErrors(), "\n\t"));
        }
        if (!messagesHolder.getErrors().isEmpty()) {
            errors.append("\nField errors:\n");
            for (Map.Entry<String, ErrorMessage> fieldError : messagesHolder.getErrors().entrySet()) {
                errors.append(String.format("\t%s -> %s%n", fieldError.getKey(), fieldError.getValue().getMessage()));
            }
        }
        warn("Entity deletion failed. Cause: %s", errors.toString());
    }

    private void warn(final String msg, final Object... args) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(String.format(msg, args));
        }
    }

    private void info(final String msg, final Object... args) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format(msg, args));
        }
    }

}
