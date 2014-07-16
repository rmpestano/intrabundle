package br.ufrgs.rmpestano.intrabundle.plugin;

import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.util.OSUtils;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.junit.Before;

import java.io.IOException;

/**
 * Created by rmpestano on 1/24/14.
 */
public abstract class BaseOSGiTest extends SingletonAbstractShellTest {

    public String newLine;

    /**
     * creates an eclipse based OSGi project with a main folder
     * and 3 sub folders inside which are OSGi bundles
     */
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

    /**
     *  creates an OSGi maven based project where we have a master module
     *  that aggregates sub modules(also maven projects) that are OSGi bundles
     */
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

    /**
     * create an OSGi project where bundles are separared by packages.
     * A real example of that is the Jitsi project:https://github.com/jitsi/jitsi
     */
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

    /**
     * OSGi project with 2 folder levels and inside 3 OSGi modules
     */
    protected void initializeOSGiProjectWithTwoFolderLevels() throws IOException {
        DirectoryResource root = createTempFolder();
        DirectoryResource level1 = root.getOrCreateChildDirectory("level1");
        DirectoryResource level2 = level1.getOrCreateChildDirectory("level2");
        addBundle(level2,"module1");
        addBundle(level2,"module2");
        addBundle(level2,"module3");
        getShell().setCurrentResource(level1);
    }

    /**
     * creates an eclipse OSGi project based on BND tools
     * which has 3 sub folders inside which are OSGi bundles
     */
    public Project initializeOSGiBNDProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addBndBundle(main,"module1");
        addBndBundle(main,"module2");
        addBndBundle(main,"module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    /**
     * creates a maven OSGi project based on BND tools(maven bundle plugin)
     * which has 3 sub folders inside which are OSGi bundles
     */
    public Project initializeMavenOSGiBNDProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addMavenBndBundle(main,"module1");
        addMavenBndBundle(main,"module2");
        addMavenBndBundle(main,"module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    private void addPom(DirectoryResource root) {
        FileResource<?> pom = (FileResource<?>) root.getChild("pom.xml");
        if(!pom.exists()){
            pom.setContents(getClass().getResourceAsStream("/pomWithDependencies.xml"));
        }
    }

    private void addBndPom(DirectoryResource root) {
        FileResource<?> pom = (FileResource<?>) root.getChild("pom.xml");
        if(!pom.exists()){
            pom.setContents(getClass().getResourceAsStream("/pom_bnd.xml"));
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

    protected void addMavenBndBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMavenActivator(bundle);
        addBndPom(bundle);
    }

    protected void addBndBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addManifest(bundle, "/bnd.bnd");
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

  public String getNewLine() {
    if(newLine == null ){
      newLine = OSUtils.isWindows() ? "\r\n":"\n";
    }
    return newLine;
  }
}
