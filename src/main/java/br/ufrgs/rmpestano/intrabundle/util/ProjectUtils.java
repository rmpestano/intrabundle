package br.ufrgs.rmpestano.intrabundle.util;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;

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
            return src.exists() ? src.getChild("main").getChild("resources"):null;
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

    public static Resource<?> getProjectMetaInfPath(DirectoryResource root) {
        Resource<?> srcPath = getProjectResourcesPath(root);
        return srcPath != null ? srcPath.getChild("META-INF"):null;
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
}
