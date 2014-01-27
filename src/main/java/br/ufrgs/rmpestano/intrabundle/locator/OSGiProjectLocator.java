package br.ufrgs.rmpestano.intrabundle.locator;

import br.ufrgs.rmpestano.intrabundle.annotation.Current;
import br.ufrgs.rmpestano.intrabundle.facet.OSGiFacet;
import br.ufrgs.rmpestano.intrabundle.i18n.ResourceBundle;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.locator.ProjectLocator;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rmpestano on 1/22/14.
 */
@Singleton
public class OSGiProjectLocator implements ProjectLocator {

    private final ProjectFactory factory;

    private final Instance<OSGiFacet> osgiFacetInstance;

    @Inject
    @Current
    Instance<ResourceBundle> resourceBundle;

    @Inject
    Shell shell;


    @Inject
    public OSGiProjectLocator(final ProjectFactory factory, @Any final Instance<OSGiFacet> osgiFacet) {
        this.factory = factory;
        this.osgiFacetInstance = osgiFacet;
    }

    @Override
    public Project createProject(DirectoryResource directoryResource) {
        OSGiFacet osgi = osgiFacetInstance.get();
        OSGiProject result = new OSGiProject(factory, directoryResource);
        osgi.setProject(result);
        if(!directoryResource.getChild("pom.xml").exists()){
            directoryResource.createNewFile();
            FileResource<?> pom = (FileResource<?>) directoryResource.getChild("pom.xml");
            pom.setContents(getClass().getResourceAsStream("/pom.xml"));
        }
        /* we are not going to install OSGi projects, only inspect existing ones
        if (!osgi.isInstalled()) {
            result.installFacet(osgi);
        } else    */
        result.registerFacet(osgi);

        if (!result.hasFacet(OSGiFacet.class)) {
            throw new IllegalStateException("Could not create OSGi project [OSGi facet could not be installed.]");
        }
        shell.println(ShellColor.YELLOW,resourceBundle.get().getString("osgi.welcome"));
        return result;
    }


    @Override
    public boolean containsProject(DirectoryResource directoryResource) {
        return osgiFacetInstance.get().isOSGiProject(directoryResource);
    }
}
