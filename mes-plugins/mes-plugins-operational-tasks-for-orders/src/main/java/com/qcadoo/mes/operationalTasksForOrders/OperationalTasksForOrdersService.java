/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.operationalTasksForOrders;

import java.util.List;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public interface OperationalTasksForOrdersService {

    /**
     * Gets tech oper comp operational task
     * 
     * @param techOperCompOperationalTaskId
     *            tech oper comp operational task id
     * 
     * @return tech oper comp operational task
     */
    Entity getTechOperCompOperationalTask(final Long techOperCompOperationalTaskId);

    /**
     * Gets tech oper comp operational task data definition
     * 
     * @return tech oper comp operational task data definition
     */
    DataDefinition getTechOperCompOperationalTaskDD();

    /**
     * Creates tech oper comp operational task
     * 
     * @param technologyOperationComponent
     *            technology operation component
     * 
     * @return tech oper comp operational task
     */
    Entity createTechOperCompOperationalTask(final Entity technologyOperationComponent);

    /**
     * Gets technology operation components for operation
     * 
     * @param operation
     *            operation
     * 
     * @return technologyOperationComponents
     */
    List<Entity> getTechnologyOperationComponentsForOperation(final Entity operation);

    /**
     * Gets tech oper comp operational tasks for technology operation component
     * 
     * @param technologyOperationComponent
     *            technology operation component
     * 
     * @return techOperComOperationalTasks
     */
    List<Entity> getTechOperCompOperationalTasksForTechnologyOperationComponent(final Entity technologyOperationComponent);

    /**
     * Gets operational tasks for tech oper comp operational tasks
     * 
     * @param techOperCompOperationalTask
     *            tech oper comp operational task
     * 
     * @return operationalTasks
     */
    List<Entity> getOperationalTasksForTechOperCompOperationalTasks(final Entity techOperCompOperationalTas);

    /**
     * Gets operational tasks for order
     * 
     * @param order
     *            order
     * 
     * @return operationalTasks
     */
    List<Entity> getOperationalTasksForOrder(final Entity order);

    /**
     * Is operational task type task other case
     * 
     * @param typeTask
     * 
     * @return boolean
     */
    boolean isOperationalTaskTypeTaskOtherCase(final String typeTask);

    /**
     * Is operational task type task execution operation in order
     * 
     * @param typeTask
     * 
     * @return boolean
     */
    boolean isOperationalTaskTypeTaskExecutionOperationInOrder(final String typeTask);

}
