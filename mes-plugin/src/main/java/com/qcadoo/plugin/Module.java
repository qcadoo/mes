package com.qcadoo.plugin;

public interface Module {

    void init(Plugin plugin);

    Module getModule();

    String getName();

    Plugin getPlugin();

    void enable();

    void disable();

}
