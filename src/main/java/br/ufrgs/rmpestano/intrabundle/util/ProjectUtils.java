package br.ufrgs.rmpestano.intrabundle.util;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jboss.forge.git.GitFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.List;

/**
 * Created by rmpestano on 2/3/14.
 */
@Singleton
public class ProjectUtils implements Serializable {

    @Inject
    ResourceFactory resourceFactory;
    
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
        Resource<?> resourcesPath = getProjectResourcesPath(root);
        if (resourcesPath != null && resourcesPath.getChild("META-INF").exists()) {
            return resourcesPath.getChild("META-INF");
        }
        return root;
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
        Resource<?> manifest = getBundleManifest(projectRoot);
        if (manifest == null) {
            return false;
        }
        RandomAccessFile randomAccessFile = null;
        try {
            File f = new File(manifest.getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f, "r");
            return hasOsgiConfig(randomAccessFile);
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

    }

    public Resource<?> getBundleManifest(DirectoryResource projectRoot) {
        if (isMavenBndProject(projectRoot)) {
            Resource<?> manifest = null;
            try {
                manifest = createManifestFromMavenBundlePlugin(projectRoot);
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
        Resource<?> manifestHome = this.getProjectManifestFolder(projectRoot);
        if (manifestHome == null || !manifestHome.exists()) {
            return null;
        }
        List<Resource<?>> manifestCandidate = manifestHome.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource.getName().toLowerCase().endsWith(".mf");
            }
        });
        if (manifestCandidate == null || manifestCandidate.isEmpty()) {
            return null;
        }
        Resource<?> manifest = manifestCandidate.get(0);
        if (manifest.exists()) {
            return manifest;
        }

        return null;
    }

    private Resource<?> createManifestFromMavenBundlePlugin(DirectoryResource projectRoot) throws IOException {
        Resource<?> pom = projectRoot.getChild("pom.xml");
        if(pom == null || !pom.exists()){
            throw new RuntimeException("Pom file with maven bundle plugin must exist to create manifest from maven bnd in project:" + projectRoot);
        }
        String name = projectRoot.getFullyQualifiedName() + File.separator + "MANIFEST.MF";
        File manifest = new File(name);
        if(!manifest.exists()){
            manifest.createNewFile();
        }
        return resourceFactory.getResourceFrom(manifest);
    }

    public boolean hasOsgiConfig(RandomAccessFile aFile) throws IOException {
        String line;
        while ((line = aFile.readLine()) != null) {
            if (line.contains("Bundle-Version")) {
                return true;
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
    private boolean isMavenBndProject(DirectoryResource root) {

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
