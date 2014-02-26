package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.maven.MavenCoreFacet;
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
public class OSGiProjectDTO implements Serializable {

    protected OSGiProject project;

    protected List<String> locations;

    //key is module.toString() and value is dependencies as strings to easy report
    //manipulation cause we are reusing subreport.jasper to print a list of Strings
    protected Map<String, List<String>> moduleDependenciesCache = new HashMap<String, List<String>>();

    protected Map<String, List<String>> moduleStaleReferencesCache = new HashMap<String, List<String>>();

    protected Long numberOfStaleReferences;

    protected String version;

    protected String revision;


    public OSGiProjectDTO() {
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


    public OSGiProjectDTO(OSGiProject project) {
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

    public String getVersion() {
        if(version == null && ((Project)project).hasFacet(MavenCoreFacet.class)) {
            MavenCoreFacet mavenCoreFacet = ((Project)project).getFacet(MavenCoreFacet.class);
            version = mavenCoreFacet.getMavenProject().getVersion();
        }
        return version;
    }


    public String getRevision() {
        if(revision == null && (ProjectUtils.isGitProject((Project) project)))  {
            revision = ProjectUtils.getProjectGitHeadRevision((Project) project);
        }
        return revision;
    }

    public OSGiProject getProject() {
        return project;
    }

    public void setProject(OSGiProject project) {
        this.project = project;
    }
}
