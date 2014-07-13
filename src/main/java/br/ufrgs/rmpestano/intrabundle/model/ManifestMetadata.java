package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rmpestano on 7/13/14.
 * <p/>
 * gather information from Manifest or BND file
 */
public class ManifestMetadata implements Serializable {

    public static final String DECLARATIVE_SERVICES = "Service-Component";
    public static final String BLUE_PRINT = "Bundle-Blueprint";
    public static final String EXPORT_PACKAGE = "Export-Package";
    public static final String IMPORT_PACKAGE = "Import-Package";
    public static final String REQUIRE_BUNDLE = "Require-Bundle";
    public static final String ACTIVATOR = "Bundle-Activator";
    public static final String BUNDLE_VERSION = "Bundle-Version";

    private List<String> importedPackages;
    private List<String> exportedPackages;
    private List<String> requiredBundles;
    private FileResource<?> activator;
    private String version;
    private DirectoryResource projectRoot;
    private ResourceFactory resourceFactory;
    private ProjectUtils projectUtils;
    private Boolean usesBlueprint;
    private Boolean usesDeclarativeServices;


    public ManifestMetadata(FileResource<?> manifestSource, DirectoryResource projectRoot, ResourceFactory resourceFactory, ProjectUtils projectUtils) {
        if (manifestSource == null) {
            throw new RuntimeException("provide source of manifest metadata such as manisfest or bnd file");
        }
        this.projectUtils = projectUtils;
        this.resourceFactory = resourceFactory;
        this.projectRoot = projectRoot;

        if (manifestSource.getName().toLowerCase().endsWith(".mf")) {
            this.readManifest(manifestSource);
        }


    }

    public List<String> getImportedPackages() {
        return importedPackages;
    }

    public List<String> getExportedPackages() {
        return exportedPackages;
    }

    public List<String> getRequiredBundles() {
        return requiredBundles;
    }

    public FileResource<?> getActivator() {
        return activator;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getUsesBlueprint() {
        return usesBlueprint;
    }

    public Boolean getUsesDeclarativeServices() {
        return usesDeclarativeServices;
    }

    private void readManifest(FileResource<?> source) {
        RandomAccessFile randomAccessFile = null;
        try {
            File f = new File(source.getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f, "r");
            this.initActivator(randomAccessFile);
            randomAccessFile.seek(0);//rewind
            this.initImportedPackages(randomAccessFile);
            randomAccessFile.seek(0);//rewind
            this.initExportedPackages(randomAccessFile);
            randomAccessFile.seek(0);
            this.initRequiredBundles(randomAccessFile);
            randomAccessFile.seek(0);
            this.initVersion(randomAccessFile);
            randomAccessFile.seek(0);
            this.initBluePrint(randomAccessFile);
            randomAccessFile.seek(0);
            initDeclarativeServices(randomAccessFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initDeclarativeServices(RandomAccessFile manifest) {
        try {
            //look into stardard location
            Resource<?> resourcesDir = projectUtils.getProjectResourcesPath(projectRoot);
            if(resourcesDir != null && resourcesDir.getChild("OSGI-INF").exists() && resourcesDir.getChild("OSGI-INF").getChild("services.xml").exists()){
                usesDeclarativeServices = Boolean.TRUE;
                return;
            }
            String line;
            while ((line = manifest.readLine()) != null) {
                if (line.contains(DECLARATIVE_SERVICES)) {
                    usesDeclarativeServices = Boolean.TRUE;
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        usesDeclarativeServices = Boolean.FALSE;
    }

    private void initBluePrint(RandomAccessFile manifest) {
            try {
                //look into stardard location
                Resource<?> resourcesDir = projectUtils.getProjectResourcesPath(projectRoot);
                if(resourcesDir != null && resourcesDir.getChild("OSGI-INF").exists() &&  resourcesDir.getChild("OSGI-INF").getChild("blueprint").exists()){
                    usesBlueprint = Boolean.TRUE;
                    return;
                }
                String line;
                while ((line = manifest.readLine()) != null) {
                    if (line.contains(BLUE_PRINT)) {
                        usesBlueprint = Boolean.TRUE;
                        return;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        usesBlueprint = Boolean.FALSE;
    }

    private void initVersion(RandomAccessFile file) {
        version = new String();
        try {
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(BUNDLE_VERSION)) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if (!"".equals(line)) {
                        version = line.substring(line.indexOf(":") + 1).trim();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }
    }

    private void initRequiredBundles(RandomAccessFile file) {
        requiredBundles = new ArrayList<String>();
        try {
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(REQUIRE_BUNDLE)) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if (!"".equals(line)) {
                        requiredBundles.addAll(Arrays.asList(line.split(",")));
                    }
                    //try to get required bundles from next lines
                    String nextLine;
                    while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                        requiredBundles.addAll(Arrays.asList(nextLine.trim().split(",")));
                    }
                    break;

                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }
    }

    private void initImportedPackages(RandomAccessFile file) {
        importedPackages = new ArrayList<String>();
        try {
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(IMPORT_PACKAGE)) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if (!"".equals(line)) {
                        importedPackages.addAll(Arrays.asList(line.split(",")));
                    }
                    //try to get packages from next lines
                    String nextLine;
                    while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                        importedPackages.addAll(Arrays.asList(nextLine.trim().split(",")));
                    }
                    break;

                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }

    }

    private void initExportedPackages(RandomAccessFile file) {
        exportedPackages = new ArrayList<String>();
        try {
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(EXPORT_PACKAGE)) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if (!"".equals(line)) {
                        exportedPackages.addAll(Arrays.asList(line.split(",")));
                    }
                    //try to get packages from next lines
                    String nextLine;
                    while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                        exportedPackages.addAll(Arrays.asList(nextLine.trim().split(",")));
                    }
                    break;

                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }

    }

    private void initActivator(RandomAccessFile randomAccessFile) throws IOException {
        String line;
        while ((line = randomAccessFile.readLine()) != null) {
            if (line.contains(ACTIVATOR)) {
                break;
            }
        }
        if (line == null) {
            activator = null;//no activator
            return;
        }
        String actvatorPath = line.trim().substring(line.indexOf(ACTIVATOR) + 18);
        actvatorPath = actvatorPath.trim().replaceAll("\\.", "/");
        if (!actvatorPath.startsWith("/")) {
            actvatorPath = "/" + actvatorPath;
        }
        activator = projectUtils.getProjectSourcePath(projectRoot) != null ? (FileResource<?>) projectUtils.getProjectSourcePath(projectRoot).getChild(actvatorPath.concat(".java")) : null;
        if (activator == null || !activator.exists()) {
            //try to infer activator path from projectRoot path
            if (actvatorPath.contains(projectRoot.getName())) {
                String[] activatoPathTokens = actvatorPath.split("/");
                String projectRootPath = projectRoot.getFullyQualifiedName().substring(0, projectRoot.getFullyQualifiedName().indexOf(activatoPathTokens[1]));
                activator = (FileResource<?>) resourceFactory.getResourceFrom(new File((projectRootPath + actvatorPath).replaceAll("//", "/").trim().concat(".java")));
            }
        }
    }


}
