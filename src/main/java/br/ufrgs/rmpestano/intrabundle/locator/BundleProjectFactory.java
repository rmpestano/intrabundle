package br.ufrgs.rmpestano.intrabundle.locator;

import br.ufrgs.rmpestano.intrabundle.facet.BundleFacet;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModuleImpl;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.addon.projects.*;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.furnace.spi.ListenerRegistration;
import org.jboss.forge.furnace.util.Predicate;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.RandomAccessFile;

/**
 * Created by rmpestano on 1/22/14.
 */
@Singleton
public class BundleProjectFactory implements ProjectFactory {

    private final ProjectFactory factory;

    private final Instance<BundleFacet> bundleFacetInstance;


    @Inject
    public BundleProjectFactory(final ProjectFactory factory, @Any final Instance<BundleFacet> bundleFacet) {
        this.factory = factory;
        this.bundleFacetInstance = bundleFacet;
    }

    @Override
    public Project createProject(DirectoryResource directoryResource) {
        BundleFacet bundle = bundleFacetInstance.get();
        OSGiModuleImpl result = new OSGiModuleImpl(factory, directoryResource);
        bundle.setFaceted(result);
        /* we are not going to install OSGi projects, only inspect existing ones
        if (!osgi.isInstalled()) {
            result.installFacet(osgi);
        } else    */
        result.register(bundle);

        if (!result.hasFacet(BundleFacet.class)) {
            return null;
        }
        /* no need to create pom for non maven project in 2.x
        if(!directoryResource.getChild("pom.xml").exists()){
            FileResource<?> pom = (FileResource<?>) directoryResource.getChild("pom.xml");
            pom.setContents(getClass().getResourceAsStream("/pom.xml"));
        } */

        /**
         * TODO print welcome message "OSGi bundle found"
         */
        //shell.println(ShellColor.YELLOW,provider.getMessage("bundle.welcome"));
        return result;
    }



    @Override
    public Project findProject(FileResource<?> fileResource) {
            Resource<?> metaInf = ProjectUtils.getProjectMetaInfPath(projectRoot);
            if(metaInf == null || !metaInf.exists()){
                return false;
            }
            Resource<?> manifest = metaInf.getChild("MANIFEST.MF");
            if(!manifest.exists()){
                return false;
            }
            RandomAccessFile randomAccessFile;
            try {
                File f = new File(manifest.getFullyQualifiedName());
                randomAccessFile = new RandomAccessFile(f,"r");
                return hasOsgiConfig(randomAccessFile);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        return null;
    }

    @Override
    public Project findProject(FileResource<?> fileResource, Predicate<Project> projectPredicate) {
        return null;
    }

    @Override
    public Project findProject(FileResource<?> fileResource, ProjectProvider projectProvider) {
        return null;
    }

    @Override
    public Project findProject(FileResource<?> fileResource, ProjectProvider projectProvider, Predicate<Project> projectPredicate) {
        return null;
    }

    @Override
    public void invalidateCaches() {

    }

    @Override
    public Project createProject(DirectoryResource directoryResource, ProjectProvider projectProvider) {
        return null;
    }

    @Override
    public Project createProject(DirectoryResource directoryResource, ProjectProvider projectProvider, Iterable<Class<? extends ProjectFacet>> classes) {
        return null;
    }

    @Override
    public boolean containsProject(FileResource<?> fileResource) {
        return false;
    }

    @Override
    public boolean containsProject(FileResource<?> fileResource, ProjectProvider projectProvider) {
        return false;
    }

    @Override
    public boolean containsProject(DirectoryResource directoryResource, FileResource<?> fileResource) {
        return false;
    }

    @Override
    public boolean containsProject(DirectoryResource directoryResource, FileResource<?> fileResource, ProjectProvider projectProvider) {
        return false;
    }

    @Override
    public Project createTempProject() throws IllegalStateException {
        return null;
    }

    @Override
    public Project createTempProject(Iterable<Class<? extends ProjectFacet>> classes) throws IllegalStateException {
        return null;
    }

    @Override
    public Project createTempProject(ProjectProvider projectProvider) {
        return null;
    }

    @Override
    public Project createTempProject(ProjectProvider projectProvider, Iterable<Class<? extends ProjectFacet>> classes) {
        return null;
    }

    @Override
    public ListenerRegistration<ProjectListener> addProjectListener(ProjectListener projectListener) {
        return null;
    }
}
