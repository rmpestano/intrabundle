package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.annotation.OSGi;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 1/22/14.
 */

@Singleton
public class OSGiFacet extends BaseFacet {

    private int metaInfSearchLevel = 2;//defines how much directory levels to go down looking for OSGi metadata(manifest file)


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
     * search OSGi metadata looking for META-INF directory with manisfest.mf file
     * containing the 'bundle' word
     *
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
     * until metaInfSearchLevel is reached
     *
     * @param parent
     * @param resourcesFound
     * @param currentLevel
     */
    public void getMetaInfDirectories(DirectoryResource parent, List<Resource<?>> resourcesFound, int currentLevel) {
        if (currentLevel >= metaInfSearchLevel) {
            return;
        }
        for (Resource<?> r : parent.listResources()) {
            if (r instanceof DirectoryResource) {
                resourcesFound.addAll(r.listResources(new ResourceFilter() {
                    @Override
                    public boolean accept(Resource<?> resource) {
                        return resource.getName().equalsIgnoreCase("meta-inf");
                    }
                }));
                getMetaInfDirectories((DirectoryResource) r, resourcesFound, ++currentLevel);
            }
        }

    }

    private boolean isOSGiModule(Resource<?> metaInf) {
        Resource<?> manifest = metaInf.getChild("MANIFEST.MF");
        if (!manifest.exists()) {
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
        String line = "";
        while ((line = aFile.readLine()) != null) {
            if (line.contains("Bundle")) {
                return true;
            }
        }
        return false;

    }

    @Produces
    @OSGi
    public OSGiProject getCurrentOSGiProject() {
        return (OSGiProject) getProject();
    }


    public int getMetaInfSearchLevel() {
        return metaInfSearchLevel;
    }

    public void setMetaInfSearchLevel(int metaInfSearchLevel) {
        this.metaInfSearchLevel = metaInfSearchLevel;
    }
}
