package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

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

    @Inject
    protected ProjectUtils projectUtils;

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
        return isOSGiProject(project.getProjectRoot());

    }

    /**
     * verify if directory contains OSGi bundles by looking at each child folder
     *
     * @param projectRoot
     * @return true if it contains at least one OSGi bundle, false otherwise
     */
    public boolean isOSGiProject(DirectoryResource projectRoot) {
         if(projectRoot.toString().equals("intrabundle")){
             /* special case where intrabundle is considered OSGi bundle cause it has OSGi bundles inside
               its source code(test folder)
             */
             return false;
         }
        //try to find bundles in child directories
        for (Resource<?> child : projectRoot.listResources()) {
            DirectoryResource directoryResource = child.reify(DirectoryResource.class);
            if(directoryResource != null){
                if(projectUtils.isOsgiBundle(directoryResource)){
                    return true;
                }
            }
        }

        //try to find bundles inside source code
        Resource<?> src = projectRoot.getChild("src").exists() ? projectRoot.getChild("src") :
                projectRoot.getChild("SRC").exists() ?  projectRoot.getChild("SRC") : null;
        if(src != null){
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
        List<Resource<?>> children = root.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof DirectoryResource;
            }
        });

        if(children != null && !children.isEmpty()){
            for (Resource<?> child : children) {
                DirectoryResource directoryResource = child.reify(DirectoryResource.class);
                if(directoryResource != null  && projectUtils.isOsgiBundle(directoryResource)){
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

    private boolean childContainsOsgiBundle(Resource<?> child) {

        for (Resource<?> resource : child.listResources()) {
            DirectoryResource dir = child.reify(DirectoryResource.class);
            if(dir != null && projectUtils.isOsgiBundle(dir)){
                return true;
            }
            else return childContainsOsgiBundle(child);
        }
        return false;
    }

    @Produces
    public OSGiProject getCurrentOSGiProject() {
        return (OSGiProject) getProject();
    }

    public BundleFacet getBundleFacet() {
        return bundleFacet.get();
    }
}