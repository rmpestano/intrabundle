package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.facet.OSGiFacet;
import org.jboss.forge.project.BaseProject;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.facets.FacetNotFoundException;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;

import javax.enterprise.inject.Typed;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 1/22/14.
 */
@Typed()
public class OSGiProject extends BaseProject {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;
    private List<OSGiModule> modules;


    public OSGiProject(final ProjectFactory factory, final DirectoryResource dir) {
        this.factory = factory;
        this.projectRoot = dir;
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
        if(modules == null){
            modules = initalizeModules();
        }
        return modules;
    }

    private List<OSGiModule> initalizeModules() {
        List<OSGiModule> modulesFound = new ArrayList<OSGiModule>();
         OSGiFacet osgi = getFacet(OSGiFacet.class);
         List<Resource<?>> metaInfList = new ArrayList<Resource<?>>();
         osgi.getMetaInfDirectories(this.getProjectRoot(),metaInfList,0);
        for (Resource<?> resource : metaInfList) {
            OSGiModule osGiModule = new OSGiModule(factory, (DirectoryResource) resource.getParent());
            modulesFound.add(osGiModule);
        }
        return modulesFound;
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
        return "OSGiProject [" + getProjectRoot() + "]";
    }


}
