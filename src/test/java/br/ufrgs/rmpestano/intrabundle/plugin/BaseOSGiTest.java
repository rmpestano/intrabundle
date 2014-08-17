package br.ufrgs.rmpestano.intrabundle.plugin;

import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.util.OSUtils;
import org.jboss.forge.test.SingletonAbstractShellTest;

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
    public Project initializeOSGiProject() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addBundle(main, "module1");
        addHelloManager((DirectoryResource) main.getChild("module1"));
        addPermissions((DirectoryResource) main.getChild("module1"));
        addBundle(main, "module2");
        addBundle(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    /**
     * creates an OSGi maven based project where we have a master module
     * that aggregates sub modules(also maven projects) that are OSGi bundles
     */
    public Project initializeOSGiMavenProject() throws Exception {
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
        DirectoryResource basePackage = main.getOrCreateChildDirectory("src");
        DirectoryResource mod1 = basePackage.getOrCreateChildDirectory("module1");
        addManifest(mod1, "/MANIFEST-in-mod1-source.MF");
        addActivatorInSource(mod1.getOrCreateChildDirectory("br").getOrCreateChildDirectory("ufrgs").getOrCreateChildDirectory("rmpestano").getOrCreateChildDirectory("activator"));

        DirectoryResource mod2 = basePackage.getOrCreateChildDirectory("module2");
        addManifest(mod2, "/MANIFEST.MF");
        addActivator(mod2);

        DirectoryResource mod3 = basePackage.getOrCreateChildDirectory("module3");
        addManifest(mod3, "/MANIFEST.MF");
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
        addBundle(level2, "module1");
        addBundle(level2, "module2");
        addBundle(level2, "module3");
        getShell().setCurrentResource(level1);
    }

    /**
     * creates an eclipse OSGi project based on BND tools
     * which has 3 sub folders inside which are OSGi bundles
     */
    public Project initializeOSGiBNDProject() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addBndBundle(main, "module1");
        addBndBundle(main, "module2");
        addBndBundle(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    /**
     * creates a maven OSGi project based on BND tools(maven bundle plugin)
     * which has 3 sub folders inside which are OSGi bundles
     */
    public Project initializeMavenOSGiBNDProject() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addMavenBndBundle(main, "module2");
        addMavenBndBundle(main, "module1");
        addMavenBndBundle(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }


    public Project initializeOSGiProjectWithTestBundles() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addTestBundle(main, "module1");
        addTestBundle(main, "module2");
        getShell().setCurrentResource(main);
        return getProject();
    }

    public Project initializeOSGiProjectWithTestCodeInsideBundles() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addTestInsideBundle(main, "module1");
        addTestInsideBundle(main, "module2");
        getShell().setCurrentResource(main);
        return getProject();
    }

    private void addPom(DirectoryResource root) {
        FileResource<?> pom = (FileResource<?>) root.getChild("pom.xml");
        if (!pom.exists()) {
            pom.setContents(getClass().getResourceAsStream("/pomWithDependencies.xml"));
        }
    }

    private void addBndPom(DirectoryResource root) {
        FileResource<?> pom = (FileResource<?>) root.getChild("pom.xml");
        if (!pom.exists()) {
            pom.setContents(getClass().getResourceAsStream("/pom_bnd.xml"));
        }
    }


    protected void addBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        addActivator(bundle);
    }

    /**
     * test code in dedicated test folder
     *
     * @param dir
     * @param module
     */
    protected void addTestBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        addActivator(bundle);
        DirectoryResource testFolder = dir.getChildDirectory("test");
        addMetaInfWithManifest(testFolder, "/MANIFEST-" + module + ".MF");
        DirectoryResource aPackage = testFolder.getChildDirectory("package");
        FileResource<?> test1 = (FileResource<?>) aPackage.getChild("TestClass1.java");
        test1.setContents("import junit.framework.Assert; public class TestClass {@Test public void aTest{}}");

        FileResource<?> test2 = (FileResource<?>) aPackage.getChild("TestClass2.java");
        test2.setContents("import org.testng.Assert; public class TestClass {@Test public void aTest{}}");
    }

    /**
     * test code inside bundle
     *
     * @param dir
     * @param module
     */
    protected void addTestInsideBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        addActivator(bundle);
        DirectoryResource testFolder = bundle.getChildDirectory("test");
        DirectoryResource aPackage = testFolder.getChildDirectory("package");
        FileResource<?> test1 = (FileResource<?>) aPackage.getChild("TestClass1.java");
        test1.setContents("import junit.framework.Assert; public class TestClass {@Test public void aTest{}}");

        FileResource<?> test2 = (FileResource<?>) aPackage.getChild("TestClass2.java");
        test2.setContents("import org.testng.Assert; public class TestClass {@Test public void aTest{}}");
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
        addManifest(metaInf, manifestName);
    }


    public void addManifest(Resource<?> parent, String manifestName) {
        FileResource<?> fileResource = (FileResource<?>) parent.getChild(manifestName);
        if (!fileResource.exists()) {
            fileResource.setContents(getClass().getResourceAsStream(manifestName));
        }
    }

    private void addActivator(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("activator");
        FileResource<?> activator = (FileResource<?>) resource.getChild("Activator.java");
        activator.setContents("activator content");
    }

    private void addActivatorInSource(DirectoryResource root) {
        FileResource<?> activator = (FileResource<?>) root.getChild("Activator.java");
        activator.setContents("activator content");
    }

    private void addMavenActivator(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("main").getOrCreateChildDirectory("java").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("activator");
        FileResource<?> activator = (FileResource<?>) resource.getChild("Activator.java");
        activator.setContents("activator content");
    }

    private void addHelloManager(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("manager");
        FileResource<?> manager = (FileResource<?>) resource.getChild("HelloManager.java");
        manager.setContents(getClass().getResourceAsStream("/HelloManager.java"));
        FileResource<?> staleManager = (FileResource<?>) resource.getChild("HelloStaleManager.java");
        staleManager.setContents(getClass().getResourceAsStream("/HelloStaleManager.java"));
    }

    private void addPermissions(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("OSGI-INF");
        FileResource<?> permissions = (FileResource<?>) resource.getChild("permissions.perm");
        permissions.setContents("permissions content");
    }


    public String getNewLine() {
        if (newLine == null) {
            newLine = OSUtils.isWindows() ? "\r\n" : "\n";
        }
        return newLine;
    }
}
