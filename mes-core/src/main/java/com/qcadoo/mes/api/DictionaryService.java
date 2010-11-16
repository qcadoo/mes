/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.api;

import java.util.List;
import java.util.Set;

/**
 * Service for getting dictionaries.
 */
public interface DictionaryService {

    /**
     * Return all values for given dictionary's name.
     * 
     * @param dictionaryName
     *            dictionary's name
     * @return the dictionary's values
     */
    List<String> values(String dictionaryName);

    /**
     * Return all defined dictionaries.
     * 
     * @return the dictionaries
     */
    Set<String> dictionaries();

}
