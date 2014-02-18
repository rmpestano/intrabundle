package br.ufrgs.rmpestano.intrabundle.util;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.List;

/**
 * Created by rmpestano on 2/3/14.
 */
public class ProjectUtils implements Serializable {

    public static boolean isMavenProject(DirectoryResource projectRoot) {
        if (projectRoot == null) {
            return false;
        }
        Resource<?> pom = projectRoot.getChild("pom.xml");
        if (pom.exists()) {
            try {
                RandomAccessFile file = new RandomAccessFile(new File(pom.getFullyQualifiedName()), "r");
                String line;
                while ((line = file.readLine()) != null) {
                    if (line.contains("<dependencies>")) {
                        return true;//minimal pom added to non maven projects doest have 'dependencies' section
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                //log ex
            }
        }
        return false;
    }

    public static Resource<?> getProjectResourcesPath(DirectoryResource projectRoot) {
        if (isMavenProject(projectRoot)) {
            Resource<?> src = projectRoot.getChild("src");
            Resource<?> main  = src.exists() ? src.getChild("main"):null;
            Resource<?> resources = main != null && main.exists() ? main.getChild("resources"):null;
            return resources != null && resources.exists() ? resources : null;
        } else {
            return projectRoot;
        }
    }

    public static Resource<?> getProjectSourcePath(DirectoryResource projectRoot) {
        if (isMavenProject(projectRoot)) {
            Resource<?> src = projectRoot.getChild("src");
            Resource<?> main = src.exists() ? src.getChild("main"):null;
            return main != null && main.exists() ? main.getChild("java"):null;
        } else {
            return projectRoot.getChild("src");
        }
    }

    public static Resource<?> getProjectManifestFolder(DirectoryResource root) {
        Resource<?> resourcesPath = getProjectResourcesPath(root);
        if(resourcesPath != null && resourcesPath.getChild("META-INF").exists()){
            return resourcesPath.getChild("META-INF");
        }
        return root;
    }


    public static boolean isInterface(FileResource<?> resource) {
        JavaSource parser = JavaParser.parse(resource.getResourceInputStream());
        return parser.isInterface();

    }

    public static Resource<?> getProjectTestPath(DirectoryResource root) {
        if (isMavenProject(root)) {
            Resource<?> srcPath = root.getChild("src");
            Resource<?> testPath = srcPath.exists() ? srcPath.getChild("test"):null;
            return testPath != null && testPath.exists() ? testPath.getChild("java"):null;
        } else {
            return root.getChild("test");
        }
    }

    public static boolean isOsgiBundle(DirectoryResource projectRoot) {
            Resource<?> manifest = getBundleManifest(projectRoot);
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

    public static Resource<?> getBundleManifest(DirectoryResource projectRoot){
        Resource<?> manifestHome = ProjectUtils.getProjectManifestFolder(projectRoot);
        if(manifestHome == null || !manifestHome.exists()){
            return null;
        }
        List<Resource<?>> manifestCandidate =  manifestHome.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource.getName().toLowerCase().endsWith(".mf");
            }
        });
        if(manifestCandidate == null || manifestCandidate.isEmpty()){
            return null;
        }
        Resource<?> manifest = manifestCandidate.get(0);
        if(!manifest.exists()){
            return manifest;
        }

        return null;
    }

    public static boolean hasOsgiConfig(RandomAccessFile aFile) throws IOException {
        String line;
        while((line = aFile.readLine())!=null){
            if(line.contains("Bundle-Version")){
                return true;
            }
        }
        return false;
    }
}
