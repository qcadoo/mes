package com.qcadoo.model.internal.module;

import com.qcadoo.model.internal.api.InternalDictionaryService;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class DictionaryModule extends Module {

    private final String name;

    private final InternalDictionaryService dictionaryService;

    public DictionaryModule(final String name, final InternalDictionaryService dictionaryService) {
        this.name = name;
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void init(final PluginState state) {
        dictionaryService.createIfNotExists(name);

        if (!PluginState.ENABLED.equals(state)) {
            disable();
        }
    }

    @Override
    public void enable() {
        // TODO
        // włącza słownik
    }

    @Override
    public void disable() {
        // TODO
        // wyłącza słownik

    }

}
