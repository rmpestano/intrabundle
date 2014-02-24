package br.ufrgs.rmpestano.intrabundle.model;

import org.jboss.forge.project.Project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rmpestano on 2/4/14.
 * used by jasper reports
 */
public class OSGiProjectDTO implements Serializable {

    protected OSGiProject project;

    protected List<String> locations;

    //key is module.toString() and value is dependencies as strings to easy report
    //manipulation cause we are reusing subreport.jasper to print a list of Strings
    protected Map<String,List<String>> moduleDependenciesCache = new HashMap<String, List<String>>();


    public OSGiProjectDTO() {
    }


    public List<String> getLocations(){
        if(locations == null){
            locations = new ArrayList<String>();
            for (OSGiModule osGiModule : project.getModules()) {
                locations.add(((Project)osGiModule).getProjectRoot().getFullyQualifiedName());
            }
        }
        return locations;
    }

     public List<String> getModuleDependencies(OSGiModule module){
         String key = module.toString();
         if(!moduleDependenciesCache.containsKey(key)){
             List<String> moduleDependencies = new ArrayList<String>();
             for (OSGiModule osGiModule: project.getModulesDependencies().get(module)) {
                 moduleDependencies.add(osGiModule.toString());
             }
             moduleDependenciesCache.put(key, moduleDependencies);
         }

         return moduleDependenciesCache.get(key);
     }

    public OSGiProjectDTO(OSGiProject project) {
        this.project = project;
    }

    public OSGiProject getProject() {
        return project;
    }

    public void setProject(OSGiProject project) {
        this.project = project;
    }
}
