/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold;

import com.qcadoo.mes.api.Entity;

/**
 * Component which can hold entity - forms.
 */
public interface SaveableComponent {

    /**
     * Return entity to save, collecting fields' values from subcomponents.
     * 
     * @param viewValue
     *            value of the component
     * @return entity
     */
    Entity getSaveableEntity(final ViewValue<Long> viewValue);

}
