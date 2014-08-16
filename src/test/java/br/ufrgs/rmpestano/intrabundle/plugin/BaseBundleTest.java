package br.ufrgs.rmpestano.intrabundle.plugin;

import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.SingletonAbstractShellTest;

/**
 * Created by rmpestano on 1/24/14.
 */
public abstract class BaseBundleTest extends SingletonAbstractShellTest {



    public Project initializeOSGiProject() throws Exception{
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("module");
        addMetaInf(main, "/MANIFEST.MF");
        addActivator(main);
        addTestClass(main);
        addAbstractClass(main);
        addInterface(main);
        addPermissions(main);
        addExportedPackage(main);
        addHelloManager(main);
        getShell().setCurrentResource(main);
        return getProject();
    }

    public Project initializeOSGiProjectWithIpojoComponent() throws Exception{
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("module");
        addMetaInf(main, "/MANIFEST.MF");
        addActivator(main);
        addTestClass(main);
        addPermissions(main);
        addExportedPackage(main);
        addHelloManager(main);
        addIpojoComponent(main);
        getShell().setCurrentResource(main);
        return getProject();
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

    private void addMetaInf(DirectoryResource root,String manifestName) {
        DirectoryResource metaInf = root.getOrCreateChildDirectory("META-INF");
        FileResource<?> fileResource = (FileResource<?>) metaInf.getChild("MANIFEST.MF");
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
        activator.setContents(getClass().getResourceAsStream("/Activator.java"));
    }

    private void addIpojoComponent(DirectoryResource root){
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("service");
        FileResource<?> activator = (FileResource<?>) resource.getChild("IpojoService.java");
        activator.setContents(getClass().getResourceAsStream("/IpojoService.java"));
    }

    private void addTestClass(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("test").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano");
        FileResource<?> testClass = (FileResource<?>) resource.getChild("TestClass.java");
        testClass.setContents("import org.junit.*; test class content");
    }

    private void addAbstractClass(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano");
        FileResource<?> abstractClass = (FileResource<?>) resource.getChild("AbstractClass.java");
        abstractClass.setContents(getClass().getResourceAsStream("/AbstractService.java"));
    }

    private void addInterface(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano");
        FileResource<?> abstractClass = (FileResource<?>) resource.getChild("AInterface.java");
        abstractClass.setContents("public interface MyInterface { }");
    }

    private void addExportedPackage(DirectoryResource root){
     DirectoryResource resource =  root.getOrCreateChildDirectory("src").
               getOrCreateChildDirectory("br").
               getOrCreateChildDirectory("ufrgs").
               getOrCreateChildDirectory("rmpestano").
               getOrCreateChildDirectory("api");
        FileResource<?> exportedInterface = (FileResource<?>)resource.getChild("ExportedClass.java");
        exportedInterface.setContents("package abcd;\n public interface Interface{}");
    }


    /**
     * creates an maven OSGi project based on BND tools(maven bundle plugin)
     */
    public Project initializeMavenOSGiBNDProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        getShell().setCurrentResource(addMavenBndBundle(root, "module1"));
        return getProject();
    }

    /**
     * creates an eclipse OSGi project based on BND tools
     */
    public Project initializeOSGiBNDProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        getShell().setCurrentResource(addBndBundle(root, "module1"));
        return getProject();
    }

    /**
     * creates an eclipse OSGi project based on BND tools where bnd file is in resources folder
     */
    public Project initializeOSGiBNDProjectWithBndInResources() throws Exception
    {
        DirectoryResource root = createTempFolder();
        getShell().setCurrentResource(addBndBundleInResources(root, "module1"));
        return getProject();
    }

    protected DirectoryResource addMavenBndBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMavenActivator(bundle);
        addBndPom(bundle);
        return bundle;
    }

    protected DirectoryResource addBndBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addActivator(bundle);
        addBnd(bundle);
        return bundle;
    }

    protected DirectoryResource addBndBundleInResources(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        DirectoryResource resources = bundle.getOrCreateChildDirectory("resources");
        addActivator(bundle);
        addBnd(resources);
        return bundle;
    }


    private void addMavenActivator(DirectoryResource root){
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("main").getOrCreateChildDirectory("java").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("activator");
        FileResource<?> activator = (FileResource<?>) resource.getChild("Activator.java");
        activator.setContents("public class Activator{}");
    }

    private void addBndPom(DirectoryResource root) {
        FileResource<?> pom = (FileResource<?>) root.getChild("pom.xml");
        if(!pom.exists()){
            pom.setContents(getClass().getResourceAsStream("/pom_bnd.xml"));
        }
    }

    private void addBnd(DirectoryResource root) {
        FileResource<?> bnd = (FileResource<?>) root.getChild("bnd.bnd");
        if(!bnd.exists()){
            bnd.setContents(getClass().getResourceAsStream("/bnd.bnd"));
        }
    }


}
