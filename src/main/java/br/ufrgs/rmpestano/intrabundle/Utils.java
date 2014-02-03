package br.ufrgs.rmpestano.intrabundle;

import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by rmpestano on 2/3/14.
 */
public class Utils {

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
                        return true;//minimal pom added to non OSGi projects doest have 'dependencies' section
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
            return (DirectoryResource) projectRoot.getChild("src").getChild("main").getChild("resources");
        } else {
            return projectRoot;
        }
    }

    public static Resource<?> getProjectSourcePath(DirectoryResource projectRoot) {
        if (isMavenProject(projectRoot)) {
            return projectRoot.getChild("src").getChild("main").getChild("java");
        } else {
            return projectRoot.getChild("src");
        }
    }

    public static Resource getProjectMetaInfPath(DirectoryResource root) {
        return  getProjectResourcesPath(root).getChild("META-INF");
    }

    public static boolean isInterface(FileResource<?> intef) {
        try {
            RandomAccessFile file = new RandomAccessFile(new File(intef.getFullyQualifiedName()), "r");
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains("interface")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }
}
