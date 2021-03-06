package br.ufrgs.rmpestano.intrabundle.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by rmpestano on 1/28/14.
 */
public interface OSGiProject extends Serializable{

    List<OSGiModule> getModules();

    Long getLinesOfCode();

    Long getLinesOfTestCode();

    Map<OSGiModule, List<OSGiModule>> getModulesDependencies();

    Map<OSGiModule, List<ModuleCycle>> getModuleCyclicDependenciesMap();

    String getRevision();

    String getVersion();

    /**
     *
     * @return max quality point a project can have
     */
    int getMaxPoints();

}
