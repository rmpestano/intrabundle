package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.addon.facets.MutableFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProvidedProjectFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;

import javax.inject.Singleton;

/**
 * Created by rmpestano on 1/22/14.
 */
@Singleton
public class BundleFacet implements ProvidedProjectFacet,MutableFacet<Project>
{
     OSGiModule currentBundle;


    @Override
    public OSGiModule getFaceted() {
        return currentBundle;
    }

    /**
     * setted by FacetFactoryImpl#create fired by BundleProjectProvider#createProject
     * @param module
     */
    @Override
    public void setFaceted(Project module) {
        this.currentBundle = (OSGiModule) module;
    }

    @Override
    public boolean install() {

        return isInstalled();
    }

    @Override
    public boolean isInstalled() {
         return getFaceted() != null && hasOSGiManifest(getFaceted().getRootDirectory());
    }

    private boolean hasOSGiManifest(DirectoryResource rootDirectory) {
        for (Resource<?> resource : rootDirectory.listResources()) {
            if(resource instanceof DirectoryResource){
                return hasOSGiManifest((DirectoryResource) resource);
            }
            if(resource instanceof FileResource && resource.getName().toLowerCase().endsWith(".mf")){
               if(ProjectUtils.isOSGiManifest(resource)){
                   return true;
               }
            }
        }

        return false;
    }

    @Override
    public boolean uninstall() {
        return false;
    }




}
