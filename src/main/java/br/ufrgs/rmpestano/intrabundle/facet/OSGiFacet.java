package br.ufrgs.rmpestano.intrabundle.facet;

import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;

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

        metaInfList = this.getMetaInfDirectories(directoryResource);

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
     * for each child directory verifying if meta-inf directory exists
     *
     * @param parent
     */
    public List<Resource<?>> getMetaInfDirectories(DirectoryResource parent) {
        List<Resource<?>> resourcesFound = new ArrayList<Resource<?>>();
        for (final Resource<?> r : parent.listResources()) {
            if (r instanceof DirectoryResource) {
                DirectoryResource dir = (DirectoryResource) r;
                if (ProjectUtils.isMavenProject(dir)){
                    Resource<?> resources = ProjectUtils.getProjectResourcesPath(dir);
                    if(resources != null && resources instanceof DirectoryResource && resources.getChild("META-INF").exists()){
                        resourcesFound.add(resources.getChild("META-INF"));
                    }
                } else{
                      if(dir.getChild("META-INF").exists()){
                          resourcesFound.add(dir.getChild("META-INF"));
                      }
                }
            }
        }
        return resourcesFound;
    }

    public boolean isOSGiModule(Resource<?> metaInf) {
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
        String line;
        while ((line = aFile.readLine()) != null) {
            if (line.contains("Bundle")) {
                return true;
            }
        }
        return false;

    }

    @Produces
    public OSGiProject getCurrentOSGiProject() {
        return (OSGiProject) getProject();
    }
}