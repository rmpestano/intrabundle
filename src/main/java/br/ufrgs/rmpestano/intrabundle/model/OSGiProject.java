package br.ufrgs.rmpestano.intrabundle.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by rmpestano on 1/28/14.
 */
public interface OSGiProject extends Serializable{

    List<OSGiModule> getModules();

    /**
     * some OSGi projects have separeted OSGi bundles to test code
     * @return
     */
    List<OSGiModule> getTestModules();

    Long getLinesOfCode();

    Long getLinesOfTestCode();

    Map<OSGiModule, List<OSGiModule>> getModulesDependencies();

}