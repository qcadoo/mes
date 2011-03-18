package com.qcadoo.model.internal.api;

import com.qcadoo.model.api.DictionaryService;

public interface InternalDictionaryService extends DictionaryService {

    void createIfNotExists(String name, String... values);

}
