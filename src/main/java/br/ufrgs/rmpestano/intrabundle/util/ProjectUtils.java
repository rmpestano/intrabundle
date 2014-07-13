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
            return resource.getName().toLowerCase().endsWith(".bnd");
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
        Resource<?> resourcesPath = getProjectResourcesPath(root);
        if (resourcesPath != null && resourcesPath.getChild("META-INF").exists() && this.hasManifest(resourcesPath.getChild("META-INF"))) {
            return resourcesPath.getChild("META-INF");
        } else if (root.getChild("META-INF").exists() && this.hasManifest(root.getChild("META-INF"))) {
            return root.getChild("META-INF");
        }
        return root;
    }

    private boolean hasManifest(Resource<?> root) {
        return root.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource.getName().toLowerCase().endsWith(".mf");
            }
        }).size() > 0;
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
        if(isEclipseBndProject(projectRoot)){

            try {
                return createManifestFromEclipseBndProject(projectRoot);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
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
        if (manifest != null && manifest.exists()) {
            return manifest;
        }

        return null;
    }

    private Resource<?> createManifestFromEclipseBndProject(DirectoryResource projectRoot) throws IOException {

        Resource<?> bnd = findBndFile(projectRoot);
        if(bnd == null){
            throw new RuntimeException("Could not find bnd file for project:" + projectRoot);
        }

        String name = projectRoot.getFullyQualifiedName() + File.separator + "MANIFEST.MF";
        File manifest = null;
        FileOutputStream fout = null;
        PrintStream stream = null;
        try {
            manifest = new File(name);
            fout = new FileOutputStream(manifest);
            stream = new PrintStream(fout);
            stream.println("Bundle-Version:1.0.0.qualifier");
            stream.flush();
        }catch (Exception e){}finally {
            if (fout != null) {
                fout.close();
            }
            if (stream != null) {
                stream.close();
            }
        }
        if(manifest.exists()){
            return resourceFactory.getResourceFrom(manifest);
        }else{
            return null;
        }
    }

    private boolean isEclipseBndProject(DirectoryResource projectRoot) {
        return findBndFile(projectRoot) != null;
    }

    private Resource<?> findBndFile(DirectoryResource projectRoot) {
        List<Resource<?>> candidates =  projectRoot.listResources();

        if(candidates == null || candidates.isEmpty()){
            //try to find bnd file in resources dir
            candidates = getProjectResourcesPath(projectRoot).listResources(BND_FILTER);
            return candidates == null || candidates.isEmpty() ? null : candidates.get(0);
        }

        return candidates.get(0);

    }

    private Resource<?> createManifestFromMavenBundlePlugin(DirectoryResource projectRoot) throws IOException {
        Resource<?> pom = projectRoot.getChild("pom.xml");
        if (pom == null || !pom.exists()) {
            throw new RuntimeException("Pom file with maven bundle plugin must exist to create manifest from maven bnd in project:" + projectRoot);
        }
        String name = projectRoot.getFullyQualifiedName() + File.separator + "MANIFEST.MF";
        File manifest = null;
        FileOutputStream fout = null;
        PrintStream stream = null;
        stream.println("Bundle-Version:1.0.0.qualifier");
        try {
            manifest = new File(name);
            fout = new FileOutputStream(manifest);
            stream = new PrintStream(fout);
            stream.flush();
        }catch (Exception e){}finally {
            if (fout != null) {
                fout.close();
            }
            if (stream != null) {
                stream.close();
            }
        }
        if(manifest.exists()){
            return resourceFactory.getResourceFrom(manifest);
        }else{
            return null;
        }


        /** INITIAL IDEA READ FROM POM.XML bundle plugin
         *  if(!manifest.exists()){
         RandomAccessFile manifestCreated = null;
         RandomAccessFile pomXml = null;
         try {
         manifest.createNewFile();
         manifestCreated = new RandomAccessFile(manifest, "rw");
         pomXml = new RandomAccessFile(pom.getFullyQualifiedName(), "r");
         String line = "";
         while ((line = pomXml.readLine()) != null){
         if(line.contains(Constants.BND.MAVEN_BUNDLE_PLUGIN)){

         //TODO
         }
         }
         }finally {
         if(pomXml != null){
         pomXml.close();
         }
         if(manifestCreated != null){
         manifestCreated.close();
         }
         }
         }
         */

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
