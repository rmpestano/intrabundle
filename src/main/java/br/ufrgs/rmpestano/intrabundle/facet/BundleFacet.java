package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

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
         return project != null && isOsgiBundle(project.getProjectRoot());
    }

    public boolean isOsgiBundle(DirectoryResource root){
        Resource<?> metaInf = findManifest(root);
        if(!metaInf.exists()){
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

    private Resource<?> findManifest(DirectoryResource root) {
        if(!isMavenProject()){
            return root.getChild("META-INF");
        }else{
           return root.getChild("src").getChild("main").getChild("resources").getChild("META-INF");
        }

    }

    private boolean isMavenProject(){
        if(project == null){
            return false;
        }
        Resource<?> pom = project.getProjectRoot().getChild("pom.xml");
        if(pom.exists()){
            try{
            RandomAccessFile file = new RandomAccessFile(new File(pom.getFullyQualifiedName()), "r");
            String line;
            while ((line = file.readLine())!= null){
                 if(line.contains("<dependencies>")){
                     return true;//minimal pom added to non OSGi projects doest have 'dependencies' section
                 }
            }
            }catch (Exception ex){
                ex.printStackTrace();
                //log ex
            }
        }
        return false;
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
        return (OSGiModule) getProject();
    }

}
