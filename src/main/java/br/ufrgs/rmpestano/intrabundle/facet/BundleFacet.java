package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.project.facets.BaseFacet;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rmpestano on 1/22/14.
 */
@Singleton
public class BundleFacet extends BaseFacet {


    @Inject
    ProjectUtils projectUtils;


    @Override
    public boolean install() {

        return isInstalled();
    }

    @Override
    public boolean isInstalled() {
         return project != null && projectUtils.isOsgiBundle(project.getProjectRoot());
    }


    @Produces
    public OSGiModule produceCurrentBundle(){
        return (OSGiModule) getProject();
    }

}
