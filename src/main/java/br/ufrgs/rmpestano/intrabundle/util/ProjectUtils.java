package br.ufrgs.rmpestano.intrabundle.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.solder.logging.Logger;

/**
 * Created by rmpestano on 2/3/14.
 */
public class ProjectUtils implements Serializable {


    private static final List<String> ignoredDirectories = new ArrayList<String>() {
        {
            add("cnf");
            add(".git");
            add(".svn");
        }
    };

    public static boolean isMavenProject(DirectoryResource projectRoot) {
        if (projectRoot == null) {
            return false;
        }
        Resource<?> pom = projectRoot.getChild("pom.xml");
        if (pom.exists()) {
            try {
                //intrabundle generates a pom.xml in project that arent maven projects(Forge limitation)
                // and generated pom <name> is 'intrabudle'
                if(pomContainsNodeValue((FileResource<?>) pom,"name","intrabundle")){
                    return false;
                }
                return true;
            } catch (Exception ex) {
                 
                return false;
            }
        }
        return false;//pom doesnt not exist so its not a maven project
    }

    public static Resource<?> getProjectResourcesPath(DirectoryResource projectRoot) {
        if (isMavenProject(projectRoot)) {
            Resource<?> src = projectRoot.getChild("src");
            Resource<?> main = src.exists() ? src.getChild("main") : null;
            Resource<?> resources = main != null && main.exists() ? main.getChild("resources") : null;
            return resources != null && resources.exists() ? resources : projectRoot;
        } else {
            Resource<?> resources = projectRoot.getChild("resources");
            if (resources.exists()) {
                return resources;
            } else {
                return projectRoot;
            }
        }
    }

    public static Resource<?> getProjectSourcePath(DirectoryResource projectRoot) {
        if (isMavenProject(projectRoot)) {
            Resource<?> src = projectRoot.getChild("src");
            Resource<?> main = src.exists() ? src.getChild("main") : null;
            return main != null && main.exists() ? main.getChild("java") : projectRoot;
        } else {
            Resource<?> src = projectRoot.getChild("src");
            if (src.exists()) {
                return projectRoot.getChild("src");
            } else {
                return projectRoot;
            }
        }
    }


    public static Resource<?> getProjectManifestFolder(DirectoryResource root) {

        //always look into META-INF first
        if(root.getChild("META-INF").exists() && hasOSGiManifest(root.getChild("META-INF"))){
            return root.getChild("META-INF");
        }

        if (isMavenBndProject(root)) {
            return root;
        }
        Resource<?> resourcesPath = getProjectResourcesPath(root);

        //META-INF in resource folder
        if (resourcesPath != null && resourcesPath.getChild("META-INF").exists() && !resourcesPath.getChild("META-INF").listResources().isEmpty()) {
            return resourcesPath.getChild("META-INF");
        } else if (root.getChild("META-INF").exists() && !root.getChild("META-INF").listResources().isEmpty()) {
            return root.getChild("META-INF");
            //META-INF in source folder
        } else if (getProjectSourcePath(root).exists() && getProjectSourcePath(root).getChild("META-INF").exists() && !getProjectSourcePath(root).getChild("META-INF").listResources().isEmpty()) {
            return getProjectSourcePath(root).getChild("META-INF");
        }

        return null;
    }

    public static boolean hasOSGiManifest(Resource<?> root) {
        if (ignoredDirectories.contains(root.getName().toLowerCase())) {
            return false;
        }
        List<Resource<?>> candidates = root.listResources(Filters.MANIFEST_FILTER);
        if (!candidates.isEmpty()) {
            for (Resource<?> candidate : candidates) {
                if (hasOsgiConfig(candidate)) {
                    return true;
                }
            }
        }

        if (isEclipseBndProject((DirectoryResource) root) && hasOsgiConfig(findBndFile(root.reify(DirectoryResource.class)))) {
            return true;
        }

        if (isMavenBndProject(root.reify(DirectoryResource.class)) && hasOsgiConfigInMavenBundlePlugin(root.getChild("pom.xml"))) {
            return true;
        }
        return false;
    }

    private static boolean hasBndFile(Resource<?> root) {
        return root != null && root.listResources(Filters.BND_FILTER).size() > 0;
    }


    public static boolean isInterface(FileResource<?> resource) {
        JavaSource parser = JavaParser.parse(resource.getResourceInputStream());
        return parser.isInterface();

    }

    public static boolean isOsgiBundle(DirectoryResource projectRoot) {
        Resource<?> manifestRoot = getProjectManifestFolder(projectRoot);
        return (manifestRoot != null && hasOSGiManifest(manifestRoot)) || (isEclipseBndProject(projectRoot)) || hasOSGiManifest(projectRoot);

    }

    /**
     * return file containing OSGi manifest data such as MANIFEST.MF, file.bnd or pom.xml
     * containing maven-bundle-plugin
     *
     * @param projectRoot
     * @return
     */
    public static Resource<?> getBundleManifestSource(DirectoryResource projectRoot) {

        if (isMavenBndProject(projectRoot)) {
            Resource<?> manifest = null;
            try {
                manifest = projectRoot.getChild("pom.xml");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Problem creating manifest from maven bundle plugin from project:" + projectRoot);
            }
            if (manifest != null && manifest.exists()) {
                return manifest;
            } else {
                throw new RuntimeException("Problem creating manifest from maven bundle plugin from project:" + projectRoot);
            }

        }
        if (isEclipseBndProject(projectRoot)) {
            return findBndFile(projectRoot);
        }

        Resource<?> manifestHome = getProjectManifestFolder(projectRoot);
        if (manifestHome == null || !manifestHome.exists()) {
            manifestHome = projectRoot;
        }
        List<Resource<?>> manifestCandidate = manifestHome.listResources(Filters.MANIFEST_FILTER);
        if (manifestCandidate == null || manifestCandidate.isEmpty()) {
            return null;
        }
        Resource<?> manifest = manifestCandidate.get(0);
        if (manifest != null && manifest.exists()) {
            return manifest;
        }

        return null;
    }


    public static boolean isEclipseBndProject(DirectoryResource projectRoot) {
        return hasBndFile(projectRoot) || hasBndFile(getProjectManifestFolder(projectRoot)) || hasBndFile(getProjectResourcesPath(projectRoot));
    }

    private static Resource<?> findBndFile(DirectoryResource projectRoot) {
        //look for .bnd in project root
        List<Resource<?>> candidates = projectRoot.listResources(Filters.BND_FILTER);

        if (candidates == null || candidates.isEmpty()) {
            //try to find bnd file in resources dir
            candidates = getProjectResourcesPath(projectRoot).listResources(Filters.BND_FILTER);
            if (candidates == null || candidates.isEmpty()) {
                //try to find bnd in META-INF
                Resource<?> manifestRoot = getProjectManifestFolder(projectRoot);
                if(manifestRoot != null){
                    candidates = manifestRoot.listResources(Filters.BND_FILTER);
                    return candidates == null || candidates.isEmpty() ? null : candidates.get(0);//.bnd in meta-inf
                }
            } else {
                return candidates.get(0);//.bnd in resources folder
            }
        }

        return candidates.get(0);//.bnd in root
    }


    public static boolean hasOsgiConfig(Resource<?> resource) {
        RandomAccessFile randomAccessFile = null;
        try {
            File f = new File(resource.getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f, "r");
            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                if (line.contains(Constants.Manifest.BUNDLE_VERSION) || line.contains(Constants.Manifest.EXPORT_PACKAGE) || line.contains(Constants.Manifest.IMPORT_PACKAGE) || line.contains(Constants.Manifest.PRIVATE_PACKAGE) || line.contains("-buildpath")) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean hasOsgiConfigInMavenBundlePlugin(Resource<?> pom) {
        try {
            Node pomXml = XMLParser.parse(pom.getResourceInputStream());
            List<Node> instructions = pomXml.get("instructions");
            if(instructions == null){
                   return false;
            }
            for (Node instruction : instructions) {
                for (Node node : instruction.getChildren()) {
                    String nodeName = node.getName();
                    if (nodeName.equals(Constants.Manifest.BUNDLE_VERSION) || nodeName.equals(Constants.Manifest.EXPORT_PACKAGE) || nodeName.equals(Constants.Manifest.IMPORT_PACKAGE) || nodeName.equals(Constants.Manifest.PRIVATE_PACKAGE) || nodeName.equals(Constants.Manifest.ACTIVATOR) || nodeName.equals(Constants.Manifest.SYMBOLIC_NAME)) {
                        return true;
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isGitProject(Project project) {
        assert (project != null);
        return project.getProjectRoot().getChild(".git").exists();
    }

    /**
     * verifies if project contains maven bundle plugin in pom.xml
     */
    public static boolean isMavenBndProject(DirectoryResource root) {

        return isMavenProject(root) && hasMavenBundlePlugin(root.getChild("pom.xml"));

    }

    private static boolean hasMavenBundlePlugin(Resource<?> pom) {
        if (!pom.exists()) {
            return false;
        }

        try {
            Node pomXml = XMLParser.parse(pom.getResourceInputStream());
            List<Node> artifactId = pomXml.get("artifactId");
            for (Node node : artifactId) {
                if (node.getText().equals(Constants.BND.MAVEN_BUNDLE_PLUGIN)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getProjectGitHeadRevision(Project project) {
        assert (project != null);
        DirectoryResource root = project.getProjectRoot();
        String lastCommit = null;
        RandomAccessFile file = null;
        String headPath = null;
        boolean revisionFound = false;
        try {
            FileResource<?> headConfig = (FileResource<?>) root.getChildDirectory(".git").getChild("HEAD");
            if(headConfig.exists()) {
                file = new RandomAccessFile(headConfig.getFullyQualifiedName(), "r");
                String line;
                while ((line = file.readLine()) != null) {
                    if (line.contains("ref:")) {
                        headPath = line.substring(line.indexOf("ref:") + 1).trim();
                        break;
                    }
                }
                if (headPath != null) {
                    file = new RandomAccessFile(root.getFullyQualifiedName()+"/.git/"+headPath, "r");
                    lastCommit = "";
                    while ((line = file.readLine()) != null) {
                        lastCommit = lastCommit + line;
                    }
                }
            }

        } catch (Exception e) {
            revisionFound = false;
        } finally {
            if(file != null){
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!revisionFound){
            Logger.getLogger(ProjectUtils.class.getSimpleName()).warn("could not read git revision for project " + project);
        }
        return lastCommit;
    }


    public static Long countFileLines(FileResource<?> resource) throws IOException {
        RandomAccessFile file = new RandomAccessFile(new File(resource.getFullyQualifiedName()), "r");
        Long total = new Long(0);
        String line;
        while ((line = file.readLine()) != null) {
            if(line.startsWith("/") || line.startsWith("*")){
                continue;
            }
            total++;
        }
        file.close();
        return total;
    }

    /**
     * scans pom.xml after an UNIQUE attribute
     * @param pom
     * @param nodeName
     * @return
     */
    public static String getValueFromPomNode(FileResource<?> pom, String nodeName) {
        Node pomXml = XMLParser.parse(pom.getResourceInputStream());
        Node attr = pomXml.getSingle(nodeName);
        if(attr != null && attr.getText() != null){
            return attr.getText().trim();
        }
        else{
            return null;
        }
    }


    /**
     * verifies if pom contain node value in node name
     * @param pom
     * @param nodeName
     * @param nodeValue
     * @return
     */
    public static boolean pomContainsNodeValue(FileResource<?> pom, String nodeName, String nodeValue){
        Node pomXml = XMLParser.parse(pom.getResourceInputStream());
        List<Node> nodes = pomXml.get(nodeName);

        for (Node node : nodes) {
            if(node.getText().trim().equalsIgnoreCase(nodeValue)){
                return true;
            }
        }

        return false;
    }
}
