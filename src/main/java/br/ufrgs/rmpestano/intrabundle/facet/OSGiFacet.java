package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.event.ProjectChangeEvent;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.util.Filters;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by rmpestano on 1/22/14.
 */

@Singleton
public class OSGiFacet extends BaseFacet {

    @Inject
    protected Instance<BundleFacet> bundleFacet;

    @Override
    public boolean install() {

        /**
         * for now we are not going to install OSGi projects,
         * just analyse existing ones
         *
         */
        return isInstalled();
    }

    @Override
    public boolean isInstalled() {
        return !project.hasFacet(BundleFacet.class) && isOSGiProject(project.getProjectRoot());

    }

    /**
     * verify if directory contains OSGi bundles by looking at each child folder
     *
     * @param projectRoot
     * @return true if it contains at least one OSGi bundle, false otherwise
     */
    public boolean isOSGiProject(DirectoryResource projectRoot) {
         if(projectRoot.toString().equals("intrabundle") || projectRoot.toString().endsWith(".tmp")){
             /* special case where intrabundle is considered OSGi bundle cause it has OSGi bundles inside
               its source code(test folder)
             */
             return false;
         }
        //try to find bundles in child directories
        for (Resource<?> child : projectRoot.listResources()) {
            DirectoryResource directoryResource = child.reify(DirectoryResource.class);
            if(directoryResource != null){
                if(ProjectUtils.isOsgiBundle(directoryResource)){
                    return true;
                }
            }
        }

        //try to find bundles inside source code
        Resource<?> src = projectRoot.getChild("src").exists() ? projectRoot.getChild("src") :
                projectRoot.getChild("SRC").exists() ?  projectRoot.getChild("SRC") : null;
        if(src != null && !ProjectUtils.isMavenProject(projectRoot)){
            if(sourceContainsOsgiBundle(src)){
                return true;
            }
            else{
                return false;
            }
        }

        return false;
    }

    private boolean sourceContainsOsgiBundle(Resource<?> root) {
        boolean result = false;

        if(root.listResources(Filters.MANIFEST_FILTER).size() > 0){
            return true;
        }

        List<Resource<?>> children = root.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof DirectoryResource;
            }
        });

        if(children != null && !children.isEmpty()){
            for (Resource<?> child : children) {
                DirectoryResource directoryResource = child.reify(DirectoryResource.class);
                if(directoryResource != null  && ProjectUtils.isOsgiBundle(directoryResource)){
                    result = true;
                    break;
                }
                else{
                    result = sourceContainsOsgiBundle(child);
                }
            }
        }

        return result;
    }


    @Produces
    public OSGiProject getCurrentOSGiProject() {
        return (OSGiProject) getProject();
    }

    public BundleFacet getBundleFacet() {
        return bundleFacet.get();
    }

    public void observeProjectChange(@Observes(notifyObserver = Reception.IF_EXISTS) ProjectChangeEvent projectChangeEvent){
           setProject((org.jboss.forge.project.Project) projectChangeEvent.getOsGiProject());
    }
}