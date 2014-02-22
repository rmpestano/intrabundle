package br.ufrgs.rmpestano.intrabundle.plugin;

import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.junit.Before;

import java.io.IOException;

/**
 * Created by rmpestano on 1/24/14.
 */
public abstract class BaseOSGiTest extends SingletonAbstractShellTest {



    public Project initializeOSGiProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addBundle(main,"module1");
        addBundle(main,"module2");
        addBundle(main,"module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    public Project initializeOSGiMavenProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addPom(main);
        addMavenBundle(main, "module1");
        addMavenBundle(main, "module2");
        addMavenBundle(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    public Project initializeOSGiProjectWithBundlesInSourceCode() throws IOException {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        DirectoryResource basePackage = main.getOrCreateChildDirectory("src").
                getChildDirectory("br").getOrCreateChildDirectory("ufrgs").
                getChildDirectory("rmpestano");
        DirectoryResource mod1 = basePackage.getOrCreateChildDirectory("module1");
        addManifest(mod1, "/MANIFEST.MF");
        addActivator(mod1);

        DirectoryResource mod2 = basePackage.getOrCreateChildDirectory("module2");
        addManifest(mod2,"/MANIFEST.MF");
        addActivator(mod2);

        DirectoryResource mod3 = basePackage.getOrCreateChildDirectory("module3");
        addManifest(mod3,"/MANIFEST.MF");
        addActivator(mod3);
        getShell().setCurrentResource(main);
        return getProject();
    }

    private void addPom(DirectoryResource root) {
        FileResource<?> pom = (FileResource<?>) root.getChild("pom.xml");
        if(!pom.exists()){
            pom.setContents(getClass().getResourceAsStream("/pomWithDependencies.xml"));
        }
    }


    protected void addBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        addActivator(bundle);
    }

    protected void addMavenBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        DirectoryResource moduleResources = bundle.getOrCreateChildDirectory("src").getOrCreateChildDirectory("main").getOrCreateChildDirectory("resources");
        addMetaInfWithManifest(moduleResources, "/MANIFEST-" + module + ".MF");
        addMavenActivator(bundle);
        addPom(bundle);
    }

    private void addMetaInfWithManifest(DirectoryResource root, String manifestName) {
        DirectoryResource metaInf = root.getOrCreateChildDirectory("META-INF");
        addManifest(metaInf,manifestName);
    }


    public void addManifest(Resource<?> parent,String manifestName){
        FileResource<?> fileResource = (FileResource<?>) parent.getChild(manifestName);
        if(!fileResource.exists()){
            fileResource.setContents(getClass().getResourceAsStream(manifestName));
        }
    }

    private void addActivator(DirectoryResource root){
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("activator");
        FileResource<?> activator = (FileResource<?>) resource.getChild("Activator.java");
        activator.setContents("activator content");
    }

    private void addMavenActivator(DirectoryResource root){
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("main").getOrCreateChildDirectory("java").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("activator");
        FileResource<?> activator = (FileResource<?>) resource.getChild("Activator.java");
        activator.setContents("activator content");
    }

    @Before
    public void initProject(){
        try {
            initializeOSGiProject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
