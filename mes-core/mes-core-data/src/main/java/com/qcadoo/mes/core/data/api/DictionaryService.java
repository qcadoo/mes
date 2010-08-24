package com.qcadoo.mes.core.data.api;

import java.util.List;
import java.util.Set;

public interface DictionaryService {

    List<String> values(String dictionaryName);

    Set<String> dictionaries();

}
