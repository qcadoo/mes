/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold;

/**
 * Component which can hold entity's id - comboboxes, forms, grids, etc.
 */
public interface SelectableComponent {

    /**
     * Return current selected entity's id.
     * 
     * @param viewValue
     *            value of the component
     * @return selected entity's id
     */
    Long getSelectedEntityId(final ViewValue<Long> viewValue);

}
