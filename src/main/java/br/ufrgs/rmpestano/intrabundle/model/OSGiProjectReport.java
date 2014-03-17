package br.ufrgs.rmpestano.intrabundle.model;

import org.jboss.forge.project.Project;
import org.jboss.forge.resources.Resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rmpestano on 2/4/14.
 * used by jasper reports
 */
public class OSGiProjectReport implements Serializable {

    protected OSGiProject project;

    protected List<String> locations;

    //key is module.toString() and value is dependencies as strings to easy report
    //manipulation cause we are reusing subreport.jasper to print a list of Strings
    protected Map<String, List<String>> moduleDependenciesCache = new HashMap<String, List<String>>();

    protected Map<String, List<String>> moduleStaleReferencesCache = new HashMap<String, List<String>>();

    protected Long numberOfStaleReferences;


    public OSGiProjectReport() {
    }


    public List<String> getLocations() {
        if (locations == null) {
            locations = new ArrayList<String>();
            for (OSGiModule osGiModule : project.getModules()) {
                locations.add(((Project) osGiModule).getProjectRoot().getFullyQualifiedName());
            }
        }
        return locations;
    }

    public List<String> getModuleDependencies(OSGiModule module) {
        String key = module.toString();
        if (!moduleDependenciesCache.containsKey(key)) {
            List<String> moduleDependencies = new ArrayList<String>();
            for (OSGiModule osGiModule : project.getModulesDependencies().get(module)) {
                moduleDependencies.add(osGiModule.toString());
            }
            moduleDependenciesCache.put(key, moduleDependencies);
        }

        return moduleDependenciesCache.get(key);
    }


    public List<String> getModuleStaleReferences(OSGiModule module) {
        String key = module.toString();
        if (!moduleStaleReferencesCache.containsKey(key)) {
            List<String> moduleStaleReferences = new ArrayList<String>();
            for (Resource<?> resource : module.getStaleReferences()) {
                moduleStaleReferences.add(resource.getName());
            }
            moduleStaleReferencesCache.put(key, moduleStaleReferences);
//          moduleStaleReferencesCache.put(key, Arrays.<String>asList(String.valueOf(module.getStaleReferences())));
        }

        return moduleStaleReferencesCache.get(key);
    }


    public OSGiProjectReport(OSGiProject project) {
        this.project = project;
    }

    public Long getNumberOfStaleReferences() {
        if (numberOfStaleReferences == null) {
            numberOfStaleReferences = new Long(0);
            for (OSGiModule osGiModule : project.getModules()) {
                numberOfStaleReferences += getModuleStaleReferences(osGiModule).size();
            }
        }
        return numberOfStaleReferences;
    }



    public OSGiProject getProject() {
        return project;
    }

    public void setProject(OSGiProject project) {
        this.project = project;
    }
}
