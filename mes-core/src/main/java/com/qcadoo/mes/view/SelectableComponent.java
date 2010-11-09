package com.qcadoo.mes.view;

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
