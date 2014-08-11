package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.project.facets.BaseFacet;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

/**
 * Created by rmpestano on 1/22/14.
 */
@Singleton
public class BundleFacet extends BaseFacet {


    @Override
    public boolean install() {

        return isInstalled();
    }

    @Override
    public boolean isInstalled() {
         return project != null && ProjectUtils.isOsgiBundle(project.getProjectRoot());
    }


    @Produces
    public OSGiModule produceCurrentBundle(){
        return (OSGiModule) getProject();
    }

}
