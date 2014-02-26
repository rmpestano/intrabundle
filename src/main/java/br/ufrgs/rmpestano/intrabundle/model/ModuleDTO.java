package br.ufrgs.rmpestano.intrabundle.model;

import java.io.Serializable;

/**
 * Created by rmpestano on 2/4/14.
 * used by jasper reports
 */
public class ModuleDTO implements Serializable {

    protected OSGiModule module;

    public ModuleDTO() {
    }

    public ModuleDTO(OSGiModule module) {
        this.module = module;
    }

    public OSGiModule getModule() {
        return module;
    }

    public void setModule(OSGiModule module) {
        this.module = module;
    }
}