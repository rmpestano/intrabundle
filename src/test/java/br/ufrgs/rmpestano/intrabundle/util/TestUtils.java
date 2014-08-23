package br.ufrgs.rmpestano.intrabundle.util;

import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.util.OSUtils;

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
        activator.setContents("activator content");
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


    public static void addBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        addActivator(bundle);
    }


    public static void addBnd(DirectoryResource root) {
        FileResource<?> bnd = (FileResource<?>) root.getChild("bnd.bnd");
        if (!bnd.exists()) {
            bnd.setContents(TestUtils.class.getResourceAsStream("/bnd.bnd"));
        }
    }

    public static void addMavenBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        DirectoryResource moduleResources = bundle.getOrCreateChildDirectory("src").getOrCreateChildDirectory("main").getOrCreateChildDirectory("resources");
        TestUtils.addMetaInfWithManifest(moduleResources, "/MANIFEST-" + module + ".MF");
        TestUtils.addMavenActivator(bundle);
        TestUtils.addPom(bundle);
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


}
