package com.qcadoo.mes.model.types;

import com.qcadoo.mes.model.DataDefinition;

/**
 * Object represents "belongs to" field type.
 */
public interface BelongsToType extends LookupedType {

    /**
     * Return data definition.
     * 
     * @return data definition
     */
    DataDefinition getDataDefinition();

    /**
     * Return lookup field name.
     * 
     * @return lookup field name
     */
    String getLookupFieldName();

    /**
     * Return true if field will be lazy loaded.
     * 
     * @return is lazy loading
     */
    boolean isLazyLoading();

}