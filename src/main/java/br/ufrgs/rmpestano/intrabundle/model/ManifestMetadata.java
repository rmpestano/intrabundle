package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.util.Constants;
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

    private List<String> importedPackages;
    private List<String> exportedPackages;
    private List<String> requiredBundles;
    private FileResource<?> activator;
    private String version;
    private DirectoryResource projectRoot;
    private ResourceFactory resourceFactory;
    private Boolean usesBlueprint;
    private Boolean usesDeclarativeServices;


    public ManifestMetadata(FileResource<?> manifestSource, DirectoryResource projectRoot, ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
        this.projectRoot = projectRoot;

        this.readManifest(manifestSource);
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
        try {
            this.initActivator(source);
            this.initImportedPackages(source);
            this.initExportedPackages(source);
            this.initRequiredBundles(source);
            this.initVersion(source);
            this.initBluePrint(source);
            this.initDeclarativeServices(source);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initDeclarativeServices(FileResource<?> manifest) throws IOException {
        RandomAccessFile file = null;
        try {
            //look into stardard location
            Resource<?> resourcesDir = ProjectUtils.getProjectResourcesPath(projectRoot);
            if (resourcesDir != null && resourcesDir.getChild("OSGI-INF").exists() && resourcesDir.getChild("OSGI-INF").getChild("services.xml").exists()) {
                usesDeclarativeServices = Boolean.TRUE;
                return;
            }
            file = new RandomAccessFile(manifest.getFullyQualifiedName(), "r");
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(Constants.Manifest.DECLARATIVE_SERVICES)) {
                    usesDeclarativeServices = Boolean.TRUE;
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                file.close();
            }
        }
        usesDeclarativeServices = Boolean.FALSE;
    }

    private void initBluePrint(FileResource<?> manifest) throws IOException {
        RandomAccessFile file = null;
        try {
            //look into stardard location
            Resource<?> resourcesDir = ProjectUtils.getProjectResourcesPath(projectRoot);
            if (resourcesDir != null && resourcesDir.getChild("OSGI-INF").exists() && resourcesDir.getChild("OSGI-INF").getChild("blueprint").exists()) {
                usesBlueprint = Boolean.TRUE;
                return;
            }
            file = new RandomAccessFile(manifest.getFullyQualifiedName(), "r");
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(Constants.Manifest.BLUE_PRINT)) {
                    usesBlueprint = Boolean.TRUE;
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                file.close();
            }
        }
        usesBlueprint = Boolean.FALSE;
    }

    private void initVersion(FileResource<?> manifest) throws IOException {
        version = "";
        RandomAccessFile file = null;
        if (ProjectUtils.isMavenBndProject(projectRoot)) {
            try {
                String value = ProjectUtils.getValueFromPomNode(manifest, Constants.Manifest.BUNDLE_VERSION);
                if (value != null) {
                    version = value.trim();
                }
            } catch (Exception ex) {
                version = null;
            }
        } else {
            try {
                String line;
                file = new RandomAccessFile(manifest.getFullyQualifiedName(), "r");
                while ((line = file.readLine()) != null) {
                    if (line.contains(Constants.Manifest.BUNDLE_VERSION)) {
                        line = line.substring(line.indexOf(':') + 1).trim();
                        if (!"".equals(line)) {
                            version = line.substring(line.indexOf(':') + 1).trim();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                //TODO log ex
                e.printStackTrace();
            } finally {
                if (file != null) {
                    file.close();
                }
            }
        }
    }

    private void initRequiredBundles(FileResource<?> source) throws IOException {
        requiredBundles = new ArrayList<String>();
        if (ProjectUtils.isMavenBndProject(projectRoot)) {
            //read OSGi metadata from pom.xml in maven bundle plugin
            try {
                String nodeValue = ProjectUtils.getValueFromPomNode(source, Constants.Manifest.REQUIRE_BUNDLE);
                if (nodeValue != null && !"".equals(nodeValue)) {
                    for (String s : Arrays.asList(nodeValue.split(","))) {
                        requiredBundles.add(s.trim());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {  //read OSGi metadata from manisfest.mf
            String line;
            File f = new File(source.getFullyQualifiedName());
            RandomAccessFile file = new RandomAccessFile(f, "r");
            try {
                while ((line = file.readLine()) != null) {
                    if (line.contains(Constants.Manifest.REQUIRE_BUNDLE)) {
                        if (line.contains(":")) {
                            line = line.substring(line.indexOf(':') + 1).trim();
                        } else {
                            line = line.substring(line.indexOf(Constants.Manifest.REQUIRE_BUNDLE) + Constants.Manifest.REQUIRE_BUNDLE.length()).trim();
                        }
                        if (line.contains("\\")) {//bnd line separator
                            line = line.replaceAll("\\\\", "");
                        }
                        if (!"".equals(line)) {
                            requiredBundles.addAll(Arrays.asList(line.split(",")));
                        }else{
                            continue;
                        }
                        //try to get packages from next lines
                        String nextLine;
                        while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                            requiredBundles.addAll(Arrays.asList(nextLine.trim().replaceAll("\\\\", "").split(",")));
                        }
                        break;

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (file != null) {
                    file.close();
                }
            }
        }
    }

    private void initImportedPackages(FileResource<?> manifest) throws IOException {
        importedPackages = new ArrayList<String>();
        RandomAccessFile file = null;
        if (ProjectUtils.isMavenBndProject(projectRoot)) {
            //read OSGi metadata from pom.xml in maven bundle plugin
            try {
                String nodeValue = ProjectUtils.getValueFromPomNode(manifest, Constants.Manifest.IMPORT_PACKAGE);
                if (nodeValue != null) {
                    for (String s : nodeValue.split(",")) {
                        importedPackages.add(s.trim());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {   //read OSGi metadata from MANIFEST.MF
            try {
                String line;
                file = new RandomAccessFile(manifest.getFullyQualifiedName(), "r");
                while ((line = file.readLine()) != null) {

                    if (line.contains(Constants.Manifest.IMPORT_PACKAGE)) {
                        if (line.contains(":")) {
                            line = line.substring(line.indexOf(':') + 1).trim();
                        } else {
                            line = line.substring(line.indexOf(Constants.Manifest.IMPORT_PACKAGE) + Constants.Manifest.IMPORT_PACKAGE.length()).trim();
                        }
                        if (line.contains("\\")) {//bnd line separator
                            line = line.replaceAll("\\\\", "");
                        }
                        if (!"".equals(line)) {
                            importedPackages.addAll(Arrays.asList(line.split(",")));
                        }else{
                            continue;
                        }
                        //try to get packages from next lines
                        String nextLine;
                        while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                            importedPackages.addAll(Arrays.asList(nextLine.trim().replaceAll("\\\\", "").split(",")));
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                //TODO log ex
                e.printStackTrace();
            } finally {
                if (file != null) {
                    file.close();
                }
            }
        }

    }

    private void initExportedPackages(FileResource<?> manifest) throws IOException {
        exportedPackages = new ArrayList<String>();
        RandomAccessFile file = null;
        if (ProjectUtils.isMavenBndProject(projectRoot)) {
            //read OSGi metadata from pom.xml in maven bundle plugin
            try {
                String nodeValue = ProjectUtils.getValueFromPomNode(manifest, Constants.Manifest.EXPORT_PACKAGE);
                if (nodeValue != null && !"".equals(nodeValue)) {
                    exportedPackages.addAll(Arrays.asList(nodeValue.split(",")));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }else{
            try {
                //read OSGi metadata from MANIFEST.MF
                String line;
                file = new RandomAccessFile(manifest.getFullyQualifiedName(), "r");
                while ((line = file.readLine()) != null) {
                    if (line.contains(Constants.Manifest.EXPORT_PACKAGE)) {
                        if (line.contains(":")) {
                            line = line.substring(line.indexOf(':') + 1).trim();
                        } else {
                            line = line.substring(line.indexOf(Constants.Manifest.EXPORT_PACKAGE) + Constants.Manifest.EXPORT_PACKAGE.length()).trim();
                        }
                        if (line.contains("\\")) {//bnd line separator
                            line = line.replaceAll("\\\\", "");
                        }
                        if (!"".equals(line)) {
                            exportedPackages.addAll(Arrays.asList(line.split(",")));
                        }else{
                            continue;
                        }
                        //try to get packages from next lines
                        String nextLine;
                        while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                            exportedPackages.addAll(Arrays.asList(nextLine.trim().replaceAll("\\\\", "").split(",")));
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                //TODO log ex
                e.printStackTrace();
            } finally {
                if (file != null) {
                    file.close();
                }
            }
        }

    }

    private void initActivator(FileResource<?> source) throws IOException {
        String activatorPath = null;
        if (ProjectUtils.isMavenBndProject(projectRoot)) {
            try {
                activatorPath = ProjectUtils.getValueFromPomNode(source, Constants.Manifest.ACTIVATOR);
            } catch (Exception ex) {
                activator = null;
            }

        } else {
            File f = new File(source.getFullyQualifiedName());
            RandomAccessFile randomAccessFile = new RandomAccessFile(f, "r");
            try {
                String line;
                while ((line = randomAccessFile.readLine()) != null) {
                    if (line.contains(Constants.Manifest.ACTIVATOR)) {
                        break;
                    }
                }
                if (line == null) {
                    activator = null;//no activator
                    return;
                }
                activatorPath = line.trim().substring(line.indexOf(Constants.Manifest.ACTIVATOR) + 18);
            } catch (Exception e) {
                activator = null;
                System.out.println("could not find activator for project:" + projectRoot.getFullyQualifiedName());
            } finally {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            }

        }//end else
        if (activatorPath != null) {
            activatorPath = activatorPath.trim().replaceAll("\\.", "/");
            if (!activatorPath.startsWith("/")) {
                activatorPath = "/" + activatorPath;
            }
            activator = ProjectUtils.getProjectSourcePath(projectRoot) != null ? (FileResource<?>) ProjectUtils.getProjectSourcePath(projectRoot).getChild(activatorPath.concat(".java")) : null;
            if (activator == null || !activator.exists()) {
                //try to infer activator path from projectRoot path
                if (activatorPath.contains(projectRoot.getName())) {
                    String[] activatoPathTokens = activatorPath.split("/");
                    try {
                        String projectRootPath = projectRoot.getFullyQualifiedName().substring(0, projectRoot.getFullyQualifiedName().indexOf(activatoPathTokens[1]));
                        activator = (FileResource<?>) resourceFactory.getResourceFrom(new File((projectRootPath + activatorPath).replaceAll("//", "/").trim().concat(".java")));
                    } catch (Exception e) {
                        System.out.println("could not find activator for project:" + projectRoot.getFullyQualifiedName());
                    }
                }
            }
        }
        if (activator != null && !activator.exists()) {
            activator = null;
        }
    }


}
