package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.facet.OSGiFacet;
import org.jboss.forge.project.BaseProject;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.facets.FacetNotFoundException;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;

import javax.enterprise.inject.Typed;
import java.util.List;

/**
 * Created by rmpestano on 1/22/14.
 */
@Typed()
public class OSGiProject extends BaseProject {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;
    private List<OSGiModule> modules;


    public OSGiProject(final ProjectFactory factory, final DirectoryResource dir, final List<OSGiModule> modules) {
        this.factory = factory;
        this.projectRoot = dir;
        this.modules = modules;
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
        return modules;
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
