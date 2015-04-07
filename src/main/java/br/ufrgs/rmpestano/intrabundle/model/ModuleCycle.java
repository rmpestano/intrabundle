package br.ufrgs.rmpestano.intrabundle.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pestano on 05/04/15.
 * A cycle is composed by a list of modules which represent the
 * cycle path
 */
public class ModuleCycle {

    private List<OSGiModule> cyclePath;
    private OSGiModule module;//module which has the cycle


    public ModuleCycle(OSGiModule module) {
        this.module = module;
        cyclePath = new ArrayList<OSGiModule>();
    }

    public List<OSGiModule> getCyclePath() {
        return cyclePath;
    }

    public void addModule(OSGiModule module){
        if(module != this.module && !getCyclePath().contains(module)){
            getCyclePath().add(module);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(module.getName()).append(",");
        for (OSGiModule module : cyclePath) {
            sb.append(module.getName()).append(",");
        }
        sb.append(module.getName());

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ModuleCycle == false){
            return false;
        }
        return this.toString().equals(((ModuleCycle)obj).toString());
    }
}
