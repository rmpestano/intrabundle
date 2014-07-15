package br.ufrgs.rmpestano.intrabundle.util;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jboss.forge.git.GitFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.build.ProjectBuilder;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 2/3/14.
 */
@Singleton
public class ProjectUtils implements Serializable {

    @Inject
    ResourceFactory resourceFactory;

    @Inject
    Instance<ProjectBuilder> projectBuilder;


    public static final ResourceFilter BND_FILTER = new ResourceFilter() {
        @Override
        public boolean accept(Resource<?> resource) {
            return (resource instanceof DirectoryResource == false) && resource.getName().toLowerCase().endsWith(".bnd");
        }
    };

    public static final ResourceFilter MANIFEST_FILTER = new ResourceFilter() {
        @Override
        public boolean accept(Resource<?> resource) {
            return (resource instanceof DirectoryResource == false) && resource.getName().toLowerCase().endsWith(".mf");
        }
    };
    private static final List<String> ignoredDirectories = new ArrayList<String>() {
        {
            add("cnf");
            add(".git");
            add(".svn");
        }
    };

    public boolean isMavenProject(DirectoryResource projectRoot) {
        if (projectRoot == null) {
            return false;
        }
        Resource<?> pom = projectRoot.getChild("pom.xml");
        if (pom.exists()) {
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(new File(pom.getFullyQualifiedName()), "r");
                String line;
                while ((line = file.readLine()) != null) {
                    if (line.contains("<artifactId>intrabundle</artifactId>")) {
                        return false;//minimal pom added to non maven projects has artifactId = 'intrabundle' and so is not a maven project
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                //log ex
            } finally {
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    public Resource<?> getProjectResourcesPath(DirectoryResource projectRoot) {
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

    public Resource<?> getProjectSourcePath(DirectoryResource projectRoot) {
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

    public Resource<?> getProjectManifestFolder(DirectoryResource root) {
        if (isMavenBndProject(root)) {
            return root;
        }
        Resource<?> resourcesPath = getProjectResourcesPath(root);
        //manifest in resources folder
        if (resourcesPath != null && resourcesPath.getChild("META-INF").exists() && this.hasOSGiManifest(resourcesPath.getChild("META-INF"))) {
            return resourcesPath.getChild("META-INF");
            //manifest in META_INF folder
        } else if (root.getChild("META-INF").exists() && this.hasOSGiManifest(root.getChild("META-INF"))) {
            return root.getChild("META-INF");
        }
        return root;
    }

    private boolean hasOSGiManifest(Resource<?> root) {
        if (ignoredDirectories.contains(root.getName().toLowerCase())) {
            return false;
        }
        List<Resource<?>> candidates = root.listResources(MANIFEST_FILTER);
        if (!candidates.isEmpty()) {
            for (Resource<?> candidate : candidates) {
                if (hasOsgiConfig(candidate)) {
                    return true;
                }
            }
        }

        if (hasBndFile(root) &&
                hasOsgiConfig(findBndFile(root.reify(DirectoryResource.class)))) {
            return true;
        }

        if (isMavenBndProject(root.reify(DirectoryResource.class)) && hasOsgiConfigInMavenBundlePlugin(root.getChild("pom.xml"))) {
            return true;
        }
        return false;
    }

    private boolean hasBndFile(Resource<?> root) {
        return root.listResources(BND_FILTER).size() > 0;
    }


    public boolean isInterface(FileResource<?> resource) {
        JavaSource parser = JavaParser.parse(resource.getResourceInputStream());
        return parser.isInterface();

    }

    public Resource<?> getProjectTestPath(DirectoryResource root) {
        if (isMavenProject(root)) {
            Resource<?> srcPath = root.getChild("src");
            Resource<?> testPath = srcPath.exists() ? srcPath.getChild("test") : null;
            return testPath != null && testPath.exists() ? testPath.getChild("java") : null;
        } else {
            return root.getChild("test");
        }
    }

    public boolean isOsgiBundle(DirectoryResource projectRoot) {
        return hasOSGiManifest(getProjectManifestFolder(projectRoot));

    }

    /**
     * return file containing OSGi manifest data such as MANIFEST.MF, file.bnd or pom.xml
     * containing maven-bundle-plugin
     *
     * @param projectRoot
     * @return
     */
    public Resource<?> getBundleManifestSource(DirectoryResource projectRoot) {
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

        Resource<?> manifestHome = this.getProjectManifestFolder(projectRoot);
        if (manifestHome == null || !manifestHome.exists()) {
            return null;
        }
        List<Resource<?>> manifestCandidate = manifestHome.listResources(MANIFEST_FILTER);
        if (manifestCandidate == null || manifestCandidate.isEmpty()) {
            return null;
        }
        Resource<?> manifest = manifestCandidate.get(0);
        if (manifest != null && manifest.exists()) {
            return manifest;
        }

        return null;
    }


    private boolean isEclipseBndProject(DirectoryResource projectRoot) {
        return hasBndFile(projectRoot) || hasBndFile(getProjectManifestFolder(projectRoot)) || hasBndFile(getProjectResourcesPath(projectRoot));
    }

    private Resource<?> findBndFile(DirectoryResource projectRoot) {
        //look for .bnd in project root
        List<Resource<?>> candidates = projectRoot.listResources(BND_FILTER);

        if (candidates == null || candidates.isEmpty()) {
            //try to find bnd file in resources dir
            candidates = getProjectResourcesPath(projectRoot).listResources(BND_FILTER);
            if (candidates == null || candidates.isEmpty()) {
                //try to finf bnd in META-INF
                candidates = getProjectManifestFolder(projectRoot).listResources(BND_FILTER);
                return candidates == null || candidates.isEmpty() ? null : candidates.get(0);//.bnd in meta-inf
            } else {
                return candidates.get(0);//.bnd in resources folder
            }
        }

        return candidates.get(0);//.bnd in root

    }


    public boolean hasOsgiConfig(Resource<?> resource) {
        RandomAccessFile randomAccessFile = null;
        try {
            File f = new File(resource.getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f, "r");
            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                if (line.contains("Bundle-Version") || line.contains("Exported-Package") || line.contains("Imported-Package") || line.contains("Private-Package") || line.contains("-buildpath")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public boolean hasOsgiConfigInMavenBundlePlugin(Resource<?> resource) {
        RandomAccessFile randomAccessFile = null;
        try {
            File f = new File(resource.getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f, "r");
            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                if (line.contains("<instructions>")) {
                    String insctruction;
                    while ((insctruction = randomAccessFile.readLine()) != null && !insctruction.contains("</instructions>")) {
                        if (insctruction.contains("<Bundle-Version>") || insctruction.contains("<Exported-Package>") || insctruction.contains("<Imported-Package>") || insctruction.contains("<Private-Package>") || insctruction.contains("<Bundle-Activator>")) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public boolean isGitProject(Project project) {
        assert (project != null);
        return project.hasFacet(GitFacet.class);
    }

    /**
     * verifies if project contains maven bundle plugin in pom.xml
     */
    public boolean isMavenBndProject(DirectoryResource root) {

        return isMavenProject(root) && hasMavenBundlePlugin(root.getChild("pom.xml"));

    }

    private boolean hasMavenBundlePlugin(Resource<?> child) {
        if (!child.exists()) {
            return false;
        }

        RandomAccessFile pom = null;

        try {
            pom = new RandomAccessFile(new File(child.getFullyQualifiedName()), "r");
            String line = "";
            while ((line = pom.readLine()) != null) {
                if (line.contains(Constants.BND.MAVEN_BUNDLE_PLUGIN)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pom != null) {
                try {
                    pom.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public String getProjectGitHeadRevision(Project project) {
        assert (project != null);
        DirectoryResource root = project.getProjectRoot();
        RevCommit commit = null;
        try {
            FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
            Repository repository = repoBuilder.setGitDir(new File(root.getChildDirectory(".git").getFullyQualifiedName())).readEnvironment().build();
            RevWalk walk = new RevWalk(repository);
            commit = walk.parseCommit(repository.resolve(org.eclipse.jgit.lib.Constants.HEAD));
        } catch (Exception e) {
            throw new RuntimeException("Problem getting git head revision from project:" + project, e);
        }
        return commit.getId().toString();
    }
}
