package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.facet.OSGiFacet;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.project.BaseProject;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.FacetNotFoundException;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import javax.enterprise.inject.Typed;
import java.util.*;

/**
 * Created by rmpestano on 1/22/14.
 */
@Typed
public class OSGiProjectImpl extends BaseProject implements OSGiProject,Project {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;
    private final ResourceFactory resourceFactory;
    private List<OSGiModule> modules;
    private Map<OSGiModule, List<OSGiModule>> moduleDependenciesMap;


    public OSGiProjectImpl() {
        factory = null;
        resourceFactory = null;
    }

    public OSGiProjectImpl(final ProjectFactory factory, final ResourceFactory resourceFactory, final DirectoryResource dir) {
        this.factory = factory;
        this.projectRoot = dir;
        this.resourceFactory = resourceFactory;
    }

    @Override
    public <F extends Facet> F getFacet(final Class<F> type) {
        try {
            return super.getFacet(type);
        } catch (FacetNotFoundException e) {
            factory.registerSingleFacet(this, type);
            return super.getFacet(type);
        }
    }

    public List<OSGiModule> getModules() {
        if (modules == null) {
            modules = initalizeModules();
        }
        return modules;
    }

    private Map<OSGiModule,List<OSGiModule>> initalizeModulesDependencies() {
        Map<OSGiModule,List<OSGiModule>> dependencies = new HashMap<OSGiModule, List<OSGiModule>>();
        for (OSGiModule m1 : getModules()) {
            dependencies.put(m1,new ArrayList<OSGiModule>());
            if(m1.getImportedPackages().isEmpty()){
                //has no module dependencies, go to next module
                continue;
            }
            for (OSGiModule m2 : getModules()) {
                if(m2.equals(m1)){
                    continue;
                }
                if(!m2.getExportedPackages().isEmpty()){
                    for (String s : m2.getExportedPackages()) {
                        if(m1.getImportedPackages().contains(s)){
                            dependencies.get(m1).add(m2);
                            break;
                        }
                    }
                }


            }
        }
        return dependencies;
    }

    private List<OSGiModule> initalizeModules() {
        List<OSGiModule> modulesFound = new ArrayList<OSGiModule>();
        OSGiFacet osgi = getFacet(OSGiFacet.class);
        List<Resource<?>> children = this.getProjectRoot().listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof DirectoryResource;
            }
        });

        for (Resource<?> child : children) {
            DirectoryResource childRoot = (DirectoryResource) child;
            if(ProjectUtils.isOsgiBundle(childRoot)){
                OSGiModule osGiModule = new OSGiModuleImpl(factory, resourceFactory,childRoot);
                modulesFound.add(osGiModule);
            }
        }

        if(modulesFound.isEmpty()){
            //try to find inside source
            Resource<?> src = projectRoot.getChild("src").exists() ? projectRoot.getChild("src") :
                    projectRoot.getChild("SRC").exists() ?  projectRoot.getChild("SRC") : null;
            modulesFound.addAll(getModulesFromSource(src));

        }

        return modulesFound;
    }

    private Collection<? extends OSGiModule> getModulesFromSource(Resource<?> root) {

        List<OSGiModule> result = new ArrayList<OSGiModule>();
        List<Resource<?>> children = root.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof DirectoryResource;
            }
        });

        if(children != null && !children.isEmpty()){
            for (Resource<?> child : children) {
                DirectoryResource directoryResource = child.reify(DirectoryResource.class);
                if(directoryResource != null && ProjectUtils.isOsgiBundle(directoryResource)){
                    result.add(new OSGiModuleImpl(factory, resourceFactory,directoryResource));
                }
                else{
                    result.addAll(getModulesFromSource(child));
                }
            }
        }

        return result;
    }

    @Override
    public DirectoryResource getProjectRoot() {
        return projectRoot;
    }

    @Override
    public boolean exists() {
        return (projectRoot != null) && projectRoot.exists();
    }

    @Override
    public String toString() {
        return getProjectRoot().toString();
    }

    public Map<OSGiModule, List<OSGiModule>> getModulesDependencies() {
        if(moduleDependenciesMap == null){
            moduleDependenciesMap = initalizeModulesDependencies();
        }
        return moduleDependenciesMap;
    }


}
