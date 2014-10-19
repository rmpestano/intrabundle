package br.ufrgs.rmpestano.intrabundle.util;

import br.ufrgs.rmpestano.intrabundle.jasper.JasperManager;
import org.jboss.forge.Root;
import org.jboss.forge.maven.locators.MavenProjectLocator;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.util.OSUtils;
import org.jboss.seam.render.RenderRoot;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.SolderRoot;

/**
 * Created by rmpestano on 8/23/14.
 */
public class TestUtils {

    private static String newLine;

    public static void addPermissions(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("OSGI-INF");
        FileResource<?> permissions = (FileResource<?>) resource.getChild("permissions.perm");
        permissions.setContents("permissions content");
    }


    public static void addActivator(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("activator");
        FileResource<?> activator = (FileResource<?>) resource.getChild("Activator.java");
        activator.setContents(TestUtils.class.getResourceAsStream("/Activator.java"));
    }


    public static void addIpojoComponent(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("service");
        FileResource<?> activator = (FileResource<?>) resource.getChild("IpojoService.java");
        activator.setContents(TestUtils.class.getResourceAsStream("/IpojoService.java"));
    }

    public static void addTestClass(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("test").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano");
        FileResource<?> testClass = (FileResource<?>) resource.getChild("TestClass.java");
        testClass.setContents("import org.junit.*; test class content");
    }

    public static void addAbstractClass(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano");
        FileResource<?> abstractClass = (FileResource<?>) resource.getChild("AbstractClass.java");
        abstractClass.setContents(TestUtils.class.getResourceAsStream("/AbstractService.java"));
    }

    public static void addInterface(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano");
        FileResource<?> abstractClass = (FileResource<?>) resource.getChild("AInterface.java");
        abstractClass.setContents("public interface MyInterface { }");
    }

    public static void addMavenBndBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMavenActivator(bundle);
        addBndPom(bundle);
    }

    public static void addBndBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addManifest(bundle, "/bnd.bnd");
    }

    public static void addMetaInfWithManifest(DirectoryResource root, String manifestName) {
        DirectoryResource metaInf = root.getOrCreateChildDirectory("META-INF");
        addManifest(metaInf, manifestName);
    }


    public static void addManifest(Resource<?> parent, String manifestName) {
        FileResource<?> fileResource = (FileResource<?>) parent.getChild(manifestName);
        if (!fileResource.exists()) {
            fileResource.setContents(TestUtils.class.getResourceAsStream(manifestName));
        }
    }

    public static void addActivatorInSource(DirectoryResource root) {
        FileResource<?> activator = (FileResource<?>) root.getChild("Activator.java");
        activator.setContents("activator content");
    }

    public static void addMavenActivator(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("main").getOrCreateChildDirectory("java").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("activator");
        FileResource<?> activator = (FileResource<?>) resource.getChild("Activator.java");
        activator.setContents("package br.ufrgs.rmpestano.activator; public class Activator {}");
    }

    public static void addExportedPackage(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("api");
        FileResource<?> exportedInterface = (FileResource<?>) resource.getChild("ExportedClass.java");
        exportedInterface.setContents("package abcd;\n public interface Interface{}");
    }


    public static void addPom(DirectoryResource root) {
        FileResource<?> pom = (FileResource<?>) root.getChild("pom.xml");
        if (!pom.exists()) {
            pom.setContents(TestUtils.class.getResourceAsStream("/pomWithDependencies.xml"));
        }
    }

    public static void addBndPom(DirectoryResource root) {
        FileResource<?> pom = (FileResource<?>) root.getChild("pom.xml");
        if (!pom.exists()) {
            pom.setContents(TestUtils.class.getResourceAsStream("/pom_bnd.xml"));
        }
    }


    public static DirectoryResource addBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        addActivator(bundle);
        return bundle;
    }


    public static void addBnd(DirectoryResource root) {
        FileResource<?> bnd = (FileResource<?>) root.getChild("bnd.bnd");
        if (!bnd.exists()) {
            bnd.setContents(TestUtils.class.getResourceAsStream("/bnd.bnd"));
        }
    }

    public static DirectoryResource addMavenBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        DirectoryResource moduleResources = bundle.getOrCreateChildDirectory("src").getOrCreateChildDirectory("main").getOrCreateChildDirectory("resources");
        TestUtils.addMetaInfWithManifest(moduleResources, "/MANIFEST-" + module + ".MF");
        TestUtils.addMavenActivator(bundle);
        TestUtils.addPom(bundle);
        return bundle;
    }

    /**
     * maven bundle with manifest in root/META-INF instead of src/main/resources/META-INF
     * @param dir
     * @param module
     */
    public static void addMavenBundleWithManifestInRoot(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        TestUtils.addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        TestUtils.addMavenActivator(bundle);
        TestUtils.addPom(bundle);
    }



    public static void addHelloManager(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("manager");
        FileResource<?> manager = (FileResource<?>) resource.getChild("HelloManager.java");
        manager.setContents(TestUtils.class.getResourceAsStream("/HelloManager.java"));
        FileResource<?> staleManager = (FileResource<?>) resource.getChild("HelloStaleManager.java");
        staleManager.setContents(TestUtils.class.getResourceAsStream("/HelloStaleManager.java"));
    }


    public static String getNewLine() {
        if (newLine == null) {
            newLine = OSUtils.isWindows() ? "\r\n" : "\n";
        }
        return newLine;
    }


    public static JavaArchive getBaseDeployment() {

        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, org.jboss.shrinkwrap.api.Filters.exclude(MavenProjectLocator.class), Root.class.getPackage())
                .addPackages(true, RenderRoot.class.getPackage())
                .addPackages(true, SolderRoot.class.getPackage())
                .addAsManifestResource(new ByteArrayAsset("<beans/>".getBytes()), ArchivePaths.create("beans.xml"))
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.facet")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.model")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.i18n")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.event")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.factory")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.util")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.jdt")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.plugin")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.metric")
                .addClass(JasperManager.class)
                        //force fixed messages for test to be independent from default locale
                .addAsResource("messages_en.properties", "messages_en.properties")
                .addAsResource("messages_en.properties", "messages_pt.properties");
    }
}
