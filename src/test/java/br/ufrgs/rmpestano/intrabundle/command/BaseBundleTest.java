package br.ufrgs.rmpestano.intrabundle.command;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by rmpestano on 1/24/14.
 */
@RunWith(Arquillian.class)
public abstract class BaseBundleTest {

    @Deployment
    @Dependencies({
            @AddonDependency(name = "org.jboss.forge.addon:maven"),
            @AddonDependency(name = "org.jboss.forge.addon:ui"),
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:shell-test-harness"),
            @AddonDependency(name = "org.jboss.forge.addon:resources"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "br.ufrgs.rmpestano:intrabundle", version = "0.1-SNAPSHOT")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
                .create(ForgeArchive.class)
                .addBeansXML()
                .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.addon:maven"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:ui"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:shell-test-harness"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:resources"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("br.ufrgs.rmpestano:intrabundle", "0.1-SNAPSHOT")
                );

        return archive;
    }

    @Inject
    protected ProjectFactory projectFactory;

    @Inject
    protected BundleCommand bundleCommand;

    @Inject
    protected ShellTest shellTest;

    protected Project project;



    public void initializeOSGiProject() throws Exception {
        if(project == null){
            Project project = projectFactory.createTempProject();
            DirectoryResource root = project.getRootDirectory();
            DirectoryResource main = root.getOrCreateChildDirectory("module");
            addMetaInf(main, "/MANIFEST.MF");
            addActivator(main);
            addTestClass(main);
            addPermissions(main);
            addExportedPackage(main);
            addHelloManager(main);
            shellTest.getShell().setCurrentResource(main);
            this.project = project;
        }


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

    private void addTestClass(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("test").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano");
        FileResource<?> testClass = (FileResource<?>) resource.getChild("TestClass.java");
        testClass.setContents("test class content");
    }

    private void addExportedPackage(DirectoryResource root){
     DirectoryResource resource =  root.getOrCreateChildDirectory("src").
               getOrCreateChildDirectory("br").
               getOrCreateChildDirectory("ufrgs").
               getOrCreateChildDirectory("rmpestano").
               getOrCreateChildDirectory("api");
        FileResource<?> exportedInterface = (FileResource<?>)resource.getChild("ExportedClass.java");
        exportedInterface.setContents("public interface Interface{}");
    }


    @Before
    public void initProject(){
        try {
            initializeOSGiProject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void resetOutput(){
        try {
            shellTest.clearScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOutput(){
        return shellTest.getStdOut();
    }
}
