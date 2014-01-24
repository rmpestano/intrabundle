package br.ufrgs.rmpestano.intrabundle.locator;

import br.ufrgs.rmpestano.intrabundle.facet.OSGiFacet;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.locator.ProjectLocator;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 1/22/14.
 */
public class OSGiProjectLocator implements ProjectLocator {

    private final ProjectFactory factory;

    private final Instance<OSGiFacet> osgiFacetInstance;


    @Inject
    public OSGiProjectLocator(final ProjectFactory factory, @Any final Instance<OSGiFacet> osgiFacet) {
        this.factory = factory;
        this.osgiFacetInstance = osgiFacet;
    }

    @Override
    public Project createProject(DirectoryResource directoryResource) {
        OSGiFacet osgi = osgiFacetInstance.get();
        List<OSGiModule> projectModules = new ArrayList<OSGiModule>();
        projectModules = osgi.getModules();
        OSGiProject result = new OSGiProject(factory, directoryResource,projectModules);
        osgi.setProject(result);
        /* we are not going to install OSGi projects, only inspect existing ones
        if (!osgi.isInstalled()) {
            result.installFacet(osgi);
        } else    */
        result.registerFacet(osgi);

        if (!result.hasFacet(OSGiFacet.class)) {
            throw new IllegalStateException("Could not create OSGi project [OSGi facet could not be installed.]");
        }

        return result;
    }

    @Override
    public boolean containsProject(DirectoryResource directoryResource) {
        return osgiFacetInstance.get().isOSGiProject(directoryResource);
    }
}
