package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;
import org.jboss.forge.shell.Shell;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 1/22/14.
 */

public class OSGiFacet extends BaseFacet {

    private static final int META_INF_SEARCH_LEVEL = 2;//go down two directory levels looking for OSGi metadata(manifest file)

    @Inject
    Shell shell;

    private List<OSGiModule> modules;

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
        shell.println("verifying OSGi Facet!");
        return isOSGiProject(project.getProjectRoot());

    }

    /**
     * search OSGi metadata looking for META-INF directory with manisfest.mf file
     * containing the 'bundle' word
     * @param directoryResource
     * @return
     */
    public boolean isOSGiProject(DirectoryResource directoryResource) {
        List<Resource<?>> metaInfList = new ArrayList<Resource<?>>();

        this.getMetaInfDirectories(directoryResource, metaInfList, 0);

        if (metaInfList.isEmpty()) {
            return false;
        }
        for (Resource<?> metaInf : metaInfList) {
            if (isOSGiModule(metaInf)) {
                return true;
            }
        }
        return false;
    }

    /**
     * gather META-INF directories by looking
     * for each @parent directory get its meta-inf directory
     * until META_INF_SEARCH_LEVEL is reached
     * @param parent
     * @param resourcesFound
     * @param currentLevel
     */
    private void getMetaInfDirectories(DirectoryResource parent,List<Resource<?>> resourcesFound, int currentLevel) {
        if(currentLevel >= META_INF_SEARCH_LEVEL){
            return;
        }
        for(Resource<?> r :parent.listResources()){
            if(r instanceof DirectoryResource){
                resourcesFound.addAll(r.listResources(new ResourceFilter() {
                    @Override
                    public boolean accept(Resource<?> resource) {
                        return resource.getName().equalsIgnoreCase("meta-inf");
                    }
                }));
                getMetaInfDirectories((DirectoryResource)r,resourcesFound,++currentLevel);
            }
        }

    }

    private boolean isOSGiModule(Resource<?> metaInf) {
        Resource<?> manifest = metaInf.getChild("MANIFEST.MF");
        if(!manifest.exists()){
            return false;
        }
        RandomAccessFile randomAccessFile;
        try {
            File f = new File(manifest.getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f, "r");
            return hasOsgiConfig(randomAccessFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasOsgiConfig(RandomAccessFile aFile) throws IOException {
        for (int i = 0; i < aFile.getChannel().size(); i++) {
            String line = aFile.readLine();
            if (line.contains("Bundle")) {
                return true;
            }
        }
        return false;
    }

    public void initializeBundles(){
        //TODO
    }

    public List<OSGiModule> getModules() {
        return modules;
    }

    public void setModules(List<OSGiModule> modules) {
        this.modules = modules;
    }
}
