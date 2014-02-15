package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.Resource;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by rmpestano on 1/22/14.
 */
@Singleton
public class BundleFacet extends AbstractFacet<OSGiModule> implements ProjectFacet {


    @Override
    public OSGiModule getFaceted() {
        return origin;
    }

    @Override
    public boolean install() {

        return isInstalled();
    }

    @Override
    public boolean isInstalled() {
         return origin != null && isOsgiBundle(origin.getRootDirectory());
    }

    @Override
    public boolean uninstall() {
        return false;
    }

    public boolean isOsgiBundle(DirectoryResource projectRoot){
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

    }


    private boolean hasOsgiConfig(RandomAccessFile aFile) throws IOException {
        for (int i = 0 ;i<aFile.getChannel().size();i++){
           String line = aFile.readLine();
           if(line.contains("Bundle-Version")){
               return true;
           }
        }
        return false;
    }

    @Produces
    public OSGiModule produceCurrentBundle(){
        return origin;
    }

}
