package br.ufrgs.rmpestano.intrabundle.factory;

import br.ufrgs.rmpestano.intrabundle.locator.BundleProjectLocator;
import br.ufrgs.rmpestano.intrabundle.locator.OSGiProjectLocator;
import org.jboss.forge.project.locator.ProjectLocator;
import org.jboss.forge.project.services.FacetFactory;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rmpestano on 8/31/14.
 * Override Forge Project factory behaviour: https://github.com/forge/core/blob/1.x/shell-api/src/main/java/org/jboss/forge/project/services/ProjectFactory.java
 * which uses bottom up approach to locate project, eg: if it doesnt find project in current
 * directory it goes to parent and so on. Intrabundle only looks into current dir
 */
@Singleton
@Specializes
public class IntrabundleProjectFactory extends ProjectFactory {

    @Inject
    public IntrabundleProjectFactory(FacetFactory facetFactory, BeanManager manager, Instance<ProjectLocator> locatorInstance) {
        super(facetFactory, manager, locatorInstance);
    }

    @Override
    public DirectoryResource locateRecursively(DirectoryResource currentDirectory, ProjectLocator locator) {

       return locateInCurrentDirectory(currentDirectory,locator);
    }

    /**
     * intrabundle will look for projects only in current dir
     *
     * @return current directory if its a project, null otherwise
     */
    private DirectoryResource locateInCurrentDirectory(DirectoryResource root, ProjectLocator locator){
        //use only intrabundle project locators(OSGi & Bundle locators)
        if ((locator instanceof BundleProjectLocator == false && locator instanceof OSGiProjectLocator == false) && !locator.containsProject(root)) {
            root = null;
        }
        return root;
    }


    //default forge behaviour
  /**
    public DirectoryResource locateRecursively(final DirectoryResource startingDirectory, final ProjectLocator locator)
    {
        DirectoryResource root = startingDirectory;
        while (!locator.containsProject(root) && (root.getParent() instanceof DirectoryResource))
        {
            root = (DirectoryResource) root.getParent();
        }
        if (!locator.containsProject(root))
        {
            root = null;
        }
        return root;
    }    */
}
