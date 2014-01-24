package br.ufrgs.rmpestano.intrabundle.model;

import org.jboss.forge.project.BaseProject;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.facets.FacetNotFoundException;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;

import javax.enterprise.inject.Typed;

/**
 * Created by rmpestano on 1/22/14.
 */
@Typed()
public class OSGiModule extends BaseProject {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;

    public OSGiModule(final ProjectFactory factory, final DirectoryResource dir) {
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
        return "OSGiModule [" + getProjectRoot() + "]";
    }
}
